package com.taller.transferencias;

import java.math.BigDecimal;

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
        try {
            Cuenta origen = repository.buscarCuenta(cuentaOrigen)
                    .orElseThrow(() -> new TransferenciaRechazadaException("Cuenta origen no existe"));
            Cuenta destino = repository.buscarCuenta(cuentaDestino)
                    .orElseThrow(() -> new TransferenciaRechazadaException("Cuenta destino no existe"));

            log.info("Transferencia solicitada: origen={} destino={} cliente={} monto={}",
                    Enmascarar.cuenta(origen.numero()), Enmascarar.cuenta(destino.numero()),
                    Enmascarar.documento(origen.documento()), monto);

            validarMonto(monto);
            validarSaldo(origen, monto);
            validarLimiteDiario(origen, monto);

            repository.actualizarSaldo(origen.numero(), origen.saldo().subtract(monto));
            repository.actualizarSaldo(destino.numero(), destino.saldo().add(monto));
            repository.guardarTransferencia(origen.numero(), destino.numero(), monto);
            auditoria.registrar(cuentaOrigen, cuentaDestino, monto, "ACEPTADA", null);

            log.info("Transferencia completada: origen={} nuevoSaldo={}",
                    Enmascarar.cuenta(origen.numero()), origen.saldo().subtract(monto));
        } catch (TransferenciaRechazadaException e) {
            auditoria.registrar(cuentaOrigen, cuentaDestino, monto, "RECHAZADA", e.getMessage());
            throw e;
        }
    }

    private void validarMonto(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
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

        BigDecimal totalHoy = repository.obtenerTotalTransferidoHoy(cliente.documento());
        BigDecimal totalConTransferencia = totalHoy.add(monto);

        if (totalConTransferencia.compareTo(cliente.limiteDiario()) > 0) {
            throw new TransferenciaRechazadaException(
                    "La transferencia supera el limite diario del cliente");
        }
    }
}
