package com.taller.transferencias;

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
    public void transferir(String cuentaOrigen, String cuentaDestino, double monto) {
        Cuenta origen = repository.buscarCuenta(cuentaOrigen)
                .orElseThrow(() -> new TransferenciaRechazadaException("Cuenta origen no existe"));
        Cuenta destino = repository.buscarCuenta(cuentaDestino)
                .orElseThrow(() -> new TransferenciaRechazadaException("Cuenta destino no existe"));

        log.info("Transferencia solicitada: origen={} destino={} cliente={} monto={}",
                origen.numero(), destino.numero(), origen.documento(), monto);

        validarMonto(monto);
        validarSaldo(origen, monto);
        validarLimiteDiario(origen, monto);

        repository.actualizarSaldo(origen.numero(), origen.saldo() - monto);
        repository.actualizarSaldo(destino.numero(), destino.saldo() + monto);
        repository.guardarTransferencia(origen.numero(), destino.numero(), monto);

        log.info("Transferencia completada: origen={} nuevoSaldo={}", origen.numero(), origen.saldo() - monto);
    }

    private void validarMonto(double monto) {
        if (monto <= 0) {
            throw new TransferenciaRechazadaException("El monto debe ser mayor a cero");
        }
    }

    private void validarSaldo(Cuenta origen, double monto) {
        if (origen.saldo() < monto) {
            log.warn("Saldo insuficiente: cuenta={} cliente={} saldo={} monto={}",
                    origen.numero(), origen.documento(), origen.saldo(), monto);
            throw new TransferenciaRechazadaException("Saldo insuficiente");
        }
    }

    private void validarLimiteDiario(Cuenta origen, double monto) {
        Cliente cliente = repository.buscarCliente(origen.documento())
                .orElseThrow(() -> new TransferenciaRechazadaException("Cliente no existe"));

        double transferidoHoy = repository.totalTransferidoHoy(cliente.documento());
        if (transferidoHoy + monto > cliente.limiteDiario()) {
            log.warn("Límite diario excedido: cuenta={} cliente={} transferidoHoy={} monto={} limite={}",
                    origen.numero(), cliente.documento(), transferidoHoy, monto, cliente.limiteDiario());
            throw new TransferenciaRechazadaException("Límite diario excedido");
        }
    }
}
