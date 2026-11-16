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

    public TransferenciaService(BancoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void transferir(String cuentaOrigen, String cuentaDestino, BigDecimal montoSolicitado) {
        Cuenta origen = repository.buscarCuenta(cuentaOrigen)
                .orElseThrow(() -> new TransferenciaRechazadaException("Cuenta origen no existe"));
        Cuenta destino = repository.buscarCuenta(cuentaDestino)
                .orElseThrow(() -> new TransferenciaRechazadaException("Cuenta destino no existe"));

        validarMonto(montoSolicitado);
        BigDecimal monto = montoSolicitado.setScale(2, RoundingMode.HALF_EVEN);

        log.info("Transferencia solicitada: origen={} destino={} cliente={} monto={}",
                origen.numero(), destino.numero(), origen.documento(), monto);

        validarSaldo(origen, monto);

        repository.actualizarSaldo(origen.numero(), origen.saldo().subtract(monto));
        repository.actualizarSaldo(destino.numero(), destino.saldo().add(monto));
        repository.guardarTransferencia(origen.numero(), destino.numero(), monto);

        log.info("Transferencia completada: origen={} nuevoSaldo={}", origen.numero(), origen.saldo().subtract(monto));
    }

    private void validarMonto(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new TransferenciaRechazadaException("El monto debe ser mayor a cero");
        }
    }

    private void validarSaldo(Cuenta origen, BigDecimal monto) {
        if (origen.saldo().compareTo(monto) < 0) {
            log.warn("Saldo insuficiente: cuenta={} cliente={} saldo={} monto={}",
                    origen.numero(), origen.documento(), origen.saldo(), monto);
            throw new TransferenciaRechazadaException("Saldo insuficiente");
        }
    }
}
