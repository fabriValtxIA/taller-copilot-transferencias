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
    void limpiar() {
        jdbc.update("DELETE FROM auditoria");
        jdbc.update("DELETE FROM transferencia");
        jdbc.update("UPDATE cuenta SET saldo = CASE numero "
                + "WHEN '0011-0457-0200987654' THEN 3200.10 "
                + "WHEN '0011-0457-0200123456' THEN 12500.50 "
                + "ELSE saldo END");
    }

    @Test
    void acepta_transferencia_dentro_del_limite_diario() {
        service.transferir(ORIGEN, DESTINO, new BigDecimal("1500.00"));

        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM auditoria WHERE resultado = 'ACEPTADA'", Integer.class))
                .isEqualTo(1);
    }

    @Test
    void rechaza_transferencia_que_supera_el_limite_diario_acumulado() {
        service.transferir(ORIGEN, DESTINO, new BigDecimal("1000.00"));

        assertThatThrownBy(() ->
                service.transferir(ORIGEN, DESTINO, new BigDecimal("501.00")))
                .isInstanceOf(TransferenciaRechazadaException.class)
                .hasMessageContaining("Supera el limite diario del cliente");

        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM auditoria WHERE resultado = 'RECHAZADA'", Integer.class))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM transferencia", Integer.class))
                .isEqualTo(1);
    }
}
