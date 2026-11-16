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

    private static final String CUENTA_ANA = "0011-0457-0200123456";   // saldo 12500.50, límite 5000.00
    private static final String CUENTA_LUIS = "0011-0457-0200987654";  // saldo 3200.10, límite 1500.00

    @Autowired
    TransferenciaService service;

    @Autowired
    BancoRepository repository;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void acepta_transferencia_y_mueve_el_monto_exacto() {
        // dado
        BigDecimal monto = new BigDecimal("0.10");
        // cuando
        service.transferir(CUENTA_ANA, CUENTA_LUIS, monto);
        service.transferir(CUENTA_ANA, CUENTA_LUIS, new BigDecimal("0.20"));
        // entonces: sin errores de redondeo
        assertThat(repository.buscarCuenta(CUENTA_ANA).orElseThrow().saldo()).isEqualByComparingTo("12500.20");
        assertThat(repository.buscarCuenta(CUENTA_LUIS).orElseThrow().saldo()).isEqualByComparingTo("3200.40");
    }

    @Test
    void rechaza_transferencia_con_monto_cero_o_negativo() {
        // dado
        BigDecimal monto = new BigDecimal("-5.00");
        // cuando / entonces
        assertThatThrownBy(() -> service.transferir(CUENTA_ANA, CUENTA_LUIS, monto))
                .isInstanceOf(TransferenciaRechazadaException.class)
                .hasMessageContaining("mayor a cero");
    }

    @Test
    void rechaza_transferencia_si_no_hay_saldo_suficiente() {
        // dado
        BigDecimal monto = new BigDecimal("3200.11");
        // cuando / entonces
        assertThatThrownBy(() -> service.transferir(CUENTA_LUIS, CUENTA_ANA, monto))
                .isInstanceOf(TransferenciaRechazadaException.class)
                .hasMessageContaining("Saldo insuficiente");
    }

    @Test
    void acepta_transferencia_por_el_saldo_exacto() {
        // dado
        repository.actualizarSaldo(CUENTA_LUIS, new BigDecimal("1000.00"));
        // cuando
        service.transferir(CUENTA_LUIS, CUENTA_ANA, new BigDecimal("1000.00"));
        // entonces
        assertThat(repository.buscarCuenta(CUENTA_LUIS).orElseThrow().saldo()).isEqualByComparingTo("0.00");
    }

    @Test
    void acepta_transferencia_igual_al_limite_diario() {
        // dado
        BigDecimal monto = new BigDecimal("1500.00");
        // cuando
        service.transferir(CUENTA_LUIS, CUENTA_ANA, monto);
        // entonces
        assertThat(repository.buscarCuenta(CUENTA_LUIS).orElseThrow().saldo()).isEqualByComparingTo("1700.10");
    }

    @Test
    void rechaza_transferencia_si_supera_limite_diario() {
        // dado
        int rechazosAntes = rechazosPorLimite();
        // cuando / entonces
        assertThatThrownBy(() -> service.transferir(CUENTA_LUIS, CUENTA_ANA, new BigDecimal("1500.01")))
                .isInstanceOf(TransferenciaRechazadaException.class)
                .hasMessageContaining("límite diario");
        assertThat(rechazosPorLimite()).isEqualTo(rechazosAntes + 1);
    }

    @Test
    void rechaza_transferencia_si_el_acumulado_del_dia_supera_el_limite() {
        // dado
        service.transferir(CUENTA_LUIS, CUENTA_ANA, new BigDecimal("1000.00"));
        // cuando / entonces
        assertThatThrownBy(() -> service.transferir(CUENTA_LUIS, CUENTA_ANA, new BigDecimal("500.01")))
                .isInstanceOf(TransferenciaRechazadaException.class)
                .hasMessageContaining("límite diario");
    }

    @Test
    void audita_las_transferencias_aceptadas() {
        // dado
        int antes = auditorias("ACEPTADA");
        // cuando
        service.transferir(CUENTA_ANA, CUENTA_LUIS, new BigDecimal("10.00"));
        // entonces
        assertThat(auditorias("ACEPTADA")).isEqualTo(antes + 1);
    }

    private int rechazosPorLimite() {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM auditoria WHERE resultado = 'RECHAZADA' AND motivo = 'Supera el límite diario'",
                Integer.class);
    }

    private int auditorias(String resultado) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM auditoria WHERE resultado = ?", Integer.class, resultado);
    }
}
