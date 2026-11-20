package com.taller.transferencias;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferenciaService {

    private static final Logger log = LoggerFactory.getLogger(TransferenciaService.class);
    private static final RoundingMode REDONDEO = RoundingMode.HALF_EVEN;

    private final BancoRepository repository;
    private final AuditoriaService auditoria;

    public TransferenciaService(BancoRepository repository, AuditoriaService auditoria) {
        this.repository = repository;
        this.auditoria = auditoria;
    }

    @Transactional
    public void transferir(String cuentaOrigen, String cuentaDestino, BigDecimal monto) {
        if (monto == null) {
            monto = BigDecimal.ZERO;
        }
        BigDecimal montoNormalizado = monto.setScale(2, REDONDEO);
        try {
            Cuenta origen = repository.buscarCuenta(cuentaOrigen)
                    .orElseThrow(() -> new TransferenciaRechazadaException("Cuenta origen no existe"));
            Cuenta destino = repository.buscarCuenta(cuentaDestino)
                    .orElseThrow(() -> new TransferenciaRechazadaException("Cuenta destino no existe"));

            log.info("Transferencia solicitada: origen={} destino={} cliente={} monto={}",
                    Enmascarar.cuenta(origen.numero()), Enmascarar.cuenta(destino.numero()),
                    Enmascarar.documento(origen.documento()), montoNormalizado);

            validarMonto(montoNormalizado);
            validarSaldo(origen, montoNormalizado);
            validarLimiteDiario(origen, montoNormalizado);

            repository.actualizarSaldo(origen.numero(), origen.saldo().subtract(montoNormalizado));
            repository.actualizarSaldo(destino.numero(), destino.saldo().add(montoNormalizado));
            repository.guardarTransferencia(origen.numero(), destino.numero(), montoNormalizado);
            auditoria.registrar(origen.numero(), destino.numero(), montoNormalizado, "ACEPTADA", "Transferencia aceptada");

            log.info("Transferencia completada: origen={} nuevoSaldo={}",
                    Enmascarar.cuenta(origen.numero()),
                    origen.saldo().subtract(montoNormalizado));
        } catch (TransferenciaRechazadaException e) {
            auditoria.registrar(cuentaOrigen, cuentaDestino, montoNormalizado, "RECHAZADA", e.getMessage());
            throw e;
        }
    }

    private void validarMonto(BigDecimal monto) {
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransferenciaRechazadaException("El monto debe ser mayor a cero");
        }
    }

    private void validarSaldo(Cuenta origen, BigDecimal monto) {
        if (origen.saldo().compareTo(monto) < 0) {
            log.warn("Saldo insuficiente: cuenta={} cliente={} saldo={} monto={}",
                    Enmascarar.cuenta(origen.numero()), Enmascarar.documento(origen.documento()),
                    origen.saldo(), monto);
            throw new TransferenciaRechazadaException("Saldo insuficiente");
        }
    }

    private void validarLimiteDiario(Cuenta origen, BigDecimal monto) {
        Cliente cliente = repository.buscarCliente(origen.documento())
                .orElseThrow(() -> new TransferenciaRechazadaException("Cliente origen no existe"));
        BigDecimal inicioDia = repository.totalTransferidoHoy(
                cliente.documento(), LocalDate.now().atStartOfDay());
        BigDecimal acumulado = inicioDia.add(monto);
        if (acumulado.compareTo(cliente.limiteDiario()) > 0) {
            throw new TransferenciaRechazadaException("Supera el limite diario del cliente");
        }
    }
}
