package com.taller.transferencias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TransferenciaServiceTest {

    private static final String ORIGEN = "0011-0457-0200987654";
    private static final String DESTINO = "0011-0457-0200123456";

    @Autowired
    private TransferenciaService service;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void rechaza_transferencia_si_supera_limite_diario() {
        // dado
        BigDecimal monto = new BigDecimal("1600.00");

        // cuando / entonces
        assertThatThrownBy(() -> service.transferir(ORIGEN, DESTINO, monto))
                .isInstanceOf(TransferenciaRechazadaException.class)
                .hasMessageContaining("Supera el límite diario");

        Integer rechazadas = jdbc.queryForObject(
                "SELECT COUNT(*) FROM auditoria WHERE resultado = 'RECHAZADA' AND monto = ?",
                Integer.class, monto);
        assertThat(rechazadas).isEqualTo(1);
    }

    @Test
    void acepta_transferencia_si_cumple_limite_diario() {
        // dado
        BigDecimal monto = new BigDecimal("1500.00");

        // cuando
        service.transferir(ORIGEN, DESTINO, monto);

        // entonces
        Integer aceptadas = jdbc.queryForObject(
                "SELECT COUNT(*) FROM auditoria WHERE resultado = 'ACEPTADA' AND monto = ?",
                Integer.class, monto);
        assertThat(aceptadas).isEqualTo(1);
    }

    @Test
    void rechaza_cuando_el_acumulado_diario_mas_el_monto_supera_limite() {
        // dado
        service.transferir(ORIGEN, DESTINO, new BigDecimal("1000.00"));

        // cuando / entonces
        assertThatThrownBy(() -> service.transferir(ORIGEN, DESTINO, new BigDecimal("501.00")))
                .isInstanceOf(TransferenciaRechazadaException.class)
                .hasMessageContaining("Supera el límite diario");
    }
}
