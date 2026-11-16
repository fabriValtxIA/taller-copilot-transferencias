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
    public void transferir(String cuentaOrigen, String cuentaDestino, BigDecimal montoSolicitado) {
        try {
            validarMonto(montoSolicitado);
            BigDecimal monto = montoSolicitado.setScale(2, RoundingMode.HALF_EVEN);

            Cuenta origen = repository.buscarCuenta(cuentaOrigen)
                    .orElseThrow(() -> new TransferenciaRechazadaException("Cuenta origen no existe"));
            Cuenta destino = repository.buscarCuenta(cuentaDestino)
                    .orElseThrow(() -> new TransferenciaRechazadaException("Cuenta destino no existe"));

            log.info("Transferencia solicitada: origen={} destino={} cliente={} monto={}",
                    Enmascarar.cuenta(origen.numero()), Enmascarar.cuenta(destino.numero()),
                    Enmascarar.documento(origen.documento()), monto);

            validarSaldo(origen, monto);
            validarLimiteDiario(origen, monto);

            repository.actualizarSaldo(origen.numero(), origen.saldo().subtract(monto));
            repository.actualizarSaldo(destino.numero(), destino.saldo().add(monto));
            repository.guardarTransferencia(origen.numero(), destino.numero(), monto);
            auditoria.registrar(origen.numero(), destino.numero(), monto, AuditoriaService.ACEPTADA, null);

            log.info("Transferencia completada: origen={}", Enmascarar.cuenta(origen.numero()));
        } catch (TransferenciaRechazadaException e) {
            auditoria.registrar(cuentaOrigen, cuentaDestino, montoSolicitado, AuditoriaService.RECHAZADA, e.getMessage());
            throw e;
        }
    }

    private void validarMonto(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new TransferenciaRechazadaException("El monto debe ser mayor a cero");
        }
    }

    private void validarSaldo(Cuenta origen, BigDecimal monto) {
        if (origen.saldo().compareTo(monto) < 0) {
            log.warn("Saldo insuficiente: cuenta={} cliente={}",
                    Enmascarar.cuenta(origen.numero()), Enmascarar.documento(origen.documento()));
            throw new TransferenciaRechazadaException("Saldo insuficiente");
        }
    }

    private void validarLimiteDiario(Cuenta origen, BigDecimal monto) {
        Cliente cliente = repository.buscarCliente(origen.documento())
                .orElseThrow(() -> new TransferenciaRechazadaException("Cliente no existe"));
        BigDecimal acumulado = repository.totalTransferidoHoy(cliente.documento()).add(monto);
        if (acumulado.compareTo(cliente.limiteDiario()) > 0) {
            log.warn("Límite diario superado: cliente={} acumulado={} limite={}",
                    Enmascarar.documento(cliente.documento()), acumulado, cliente.limiteDiario());
            throw new TransferenciaRechazadaException("Supera el límite diario");
        }
    }
}
