package com.taller.transferencias;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferenciaService {

    private static final Logger log = LoggerFactory.getLogger(TransferenciaService.class);

    private final BancoRepository repository;
    private final AuditoriaService auditoria;

    public TransferenciaService(BancoRepository repository, AuditoriaService auditoria) {
        this.repository = repository;
        this.auditoria = auditoria;
    }

    @Transactional
    public void transferir(String cuentaOrigen, String cuentaDestino, BigDecimal monto) {
        BigDecimal montoNormalizado = BigDecimal.ZERO;
        Cuenta origen = null;
        Cuenta destino = null;
        try {
            montoNormalizado = normalizar(monto);
            origen = repository.buscarCuenta(cuentaOrigen)
                    .orElseThrow(() -> new TransferenciaRechazadaException("Cuenta origen no existe"));
            destino = repository.buscarCuenta(cuentaDestino)
                    .orElseThrow(() -> new TransferenciaRechazadaException("Cuenta destino no existe"));

            log.info("Transferencia solicitada: origen={} destino={} cliente={} monto={}",
                    Enmascarar.cuenta(origen.numero()), Enmascarar.cuenta(destino.numero()),
                    Enmascarar.documento(origen.documento()), montoNormalizado);

            validarMonto(montoNormalizado);
            validarSaldo(origen, montoNormalizado);
            validarLimiteDiario(origen, montoNormalizado);

            BigDecimal nuevoSaldoOrigen = normalizar(origen.saldo().subtract(montoNormalizado));
            BigDecimal nuevoSaldoDestino = normalizar(destino.saldo().add(montoNormalizado));
            repository.actualizarSaldo(origen.numero(), nuevoSaldoOrigen);
            repository.actualizarSaldo(destino.numero(), nuevoSaldoDestino);
            repository.guardarTransferencia(origen.numero(), destino.numero(), montoNormalizado);
            auditoria.registrar(origen.numero(), destino.numero(), montoNormalizado, "ACEPTADA", "Transferencia aceptada");

            log.info("Transferencia completada: origen={} nuevoSaldo={}",
                    Enmascarar.cuenta(origen.numero()), nuevoSaldoOrigen);
        } catch (TransferenciaRechazadaException ex) {
            String origenAuditado = origen == null ? cuentaOrigen : origen.numero();
            String destinoAuditado = destino == null ? cuentaDestino : destino.numero();
            auditoria.registrar(origenAuditado, destinoAuditado, montoNormalizado, "RECHAZADA", ex.getMessage());
            throw ex;
        }
    }

    private BigDecimal normalizar(BigDecimal monto) {
        if (monto == null) {
            throw new TransferenciaRechazadaException("El monto debe ser mayor a cero");
        }
        return monto.setScale(2, RoundingMode.HALF_EVEN);
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
                .orElseThrow(() -> new TransferenciaRechazadaException("Cliente no existe"));
        BigDecimal acumuladoHoy = normalizar(repository.totalTransferidoHoyPorCliente(origen.documento()));
        BigDecimal totalPropuesto = normalizar(acumuladoHoy.add(monto));

        if (totalPropuesto.compareTo(cliente.limiteDiario()) > 0) {
            throw new TransferenciaRechazadaException("Supera el límite diario");
        }
    }
}
