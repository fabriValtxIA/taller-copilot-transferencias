package com.taller.transferencias;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaService {

    public static final String ACEPTADA = "ACEPTADA";
    public static final String RECHAZADA = "RECHAZADA";

    private final JdbcTemplate jdbc;

    public AuditoriaService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Transacción propia: el rechazo queda auditado aunque la transferencia se revierta. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String origen, String destino, BigDecimal monto, String resultado, String motivo) {
        jdbc.update("""
                INSERT INTO auditoria (fecha, cuenta_origen, cuenta_destino, monto, resultado, motivo)
                VALUES (?, ?, ?, ?, ?, ?)""",
                LocalDateTime.now(), origen, destino, monto, resultado, motivo);
    }
}
