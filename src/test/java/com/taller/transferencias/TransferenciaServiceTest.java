package com.taller.transferencias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class TransferenciaServiceTest {

    private static final String ORIGEN = "0011-0457-0200987654";
    private static final String DESTINO = "0011-0457-0200123456";

    @Autowired
    private TransferenciaService service;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void limpiarOperaciones() {
        jdbc.update("DELETE FROM auditoria");
        jdbc.update("DELETE FROM transferencia");
        jdbc.update("UPDATE cuenta SET saldo = 3200.10 WHERE numero = ?", ORIGEN);
        jdbc.update("UPDATE cuenta SET saldo = 12500.50 WHERE numero = ?", DESTINO);
    }

    @Test
    void rechaza_transferencia_si_supera_limite_diario() {
        assertThatThrownBy(() -> service.transferir(
                ORIGEN, DESTINO, new BigDecimal("1600.00")))
                .isInstanceOf(TransferenciaRechazadaException.class)
                .hasMessageContaining("límite diario");

        Integer auditoriasRechazadas = jdbc.queryForObject("""
                SELECT COUNT(*) FROM auditoria
                WHERE resultado = 'RECHAZADA'
                  AND motivo LIKE '%límite diario%'
                """, Integer.class);
        assertThat(auditoriasRechazadas).isEqualTo(1);
    }

    @Test
    void acepta_transferencia_si_no_supera_limite_diario() {
        service.transferir(ORIGEN, DESTINO, new BigDecimal("1000.00"));

        Integer auditoriasAceptadas = jdbc.queryForObject("""
                SELECT COUNT(*) FROM auditoria
                WHERE resultado = 'ACEPTADA'
                """, Integer.class);
        assertThat(auditoriasAceptadas).isEqualTo(1);
    }
}
