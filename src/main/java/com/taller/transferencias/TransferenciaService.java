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
        if (monto == null) {
            auditoria.registrar(cuentaOrigen, cuentaDestino, null, "RECHAZADA", "El monto es requerido");
            throw new TransferenciaRechazadaException("El monto es requerido");
        }
        BigDecimal montoConEscala = monto.setScale(2, RoundingMode.HALF_EVEN);
        try {
            Cuenta origen = repository.buscarCuenta(cuentaOrigen)
                    .orElseThrow(() -> new TransferenciaRechazadaException("Cuenta origen no existe"));
            Cuenta destino = repository.buscarCuenta(cuentaDestino)
                    .orElseThrow(() -> new TransferenciaRechazadaException("Cuenta destino no existe"));

            log.info("Transferencia solicitada: origen={} destino={} cliente={} monto={}",
                    Enmascarar.cuenta(origen.numero()), Enmascarar.cuenta(destino.numero()),
                    Enmascarar.documento(origen.documento()), montoConEscala);

            validarMonto(montoConEscala);
            validarSaldo(origen, montoConEscala);
            validarLimiteDiario(origen, montoConEscala);

            repository.actualizarSaldo(origen.numero(), origen.saldo().subtract(montoConEscala));
            repository.actualizarSaldo(destino.numero(), destino.saldo().add(montoConEscala));
            repository.guardarTransferencia(origen.numero(), destino.numero(), montoConEscala);
            auditoria.registrar(origen.numero(), destino.numero(), montoConEscala, "ACEPTADA", "Transferencia completada");

            log.info("Transferencia completada: origen={} nuevoSaldo={}",
                    Enmascarar.cuenta(origen.numero()), origen.saldo().subtract(montoConEscala));
        } catch (TransferenciaRechazadaException e) {
            auditoria.registrar(cuentaOrigen, cuentaDestino, montoConEscala, "RECHAZADA", e.getMessage());
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
        BigDecimal transferidoHoy = repository.obtenerTransferidoHoyPorCliente(cliente.documento());
        BigDecimal totalDelDia = transferidoHoy.add(monto);

        if (totalDelDia.compareTo(cliente.limiteDiario()) > 0) {
            throw new TransferenciaRechazadaException("El límite diario del cliente fue excedido");
        }
    }
}
