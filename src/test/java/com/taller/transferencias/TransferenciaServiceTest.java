package com.taller.transferencias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TransferenciaServiceTest {

    private static final String CUENTA_ANA = "0011-0457-0200123456";   // saldo 12500.50
    private static final String CUENTA_LUIS = "0011-0457-0200987654";  // saldo 3200.10

    @Autowired
    TransferenciaService service;

    @Autowired
    BancoRepository repository;

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
        BigDecimal monto = new BigDecimal("3200.10");
        // cuando
        service.transferir(CUENTA_LUIS, CUENTA_ANA, monto);
        // entonces
        assertThat(repository.buscarCuenta(CUENTA_LUIS).orElseThrow().saldo()).isEqualByComparingTo("0.00");
    }
}
