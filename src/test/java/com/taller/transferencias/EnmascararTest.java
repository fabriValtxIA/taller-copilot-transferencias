package com.taller.transferencias;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EnmascararTest {

    @Test
    void deja_visibles_solo_los_ultimos_4_de_la_cuenta() {
        // dado
        String cuenta = "0011-0457-0200123456";
        // cuando
        String enmascarada = Enmascarar.cuenta(cuenta);
        // entonces
        assertThat(enmascarada).isEqualTo("****************3456").doesNotContain("0200123456");
    }

    @Test
    void oculta_por_completo_valores_cortos_o_nulos() {
        // dado / cuando / entonces
        assertThat(Enmascarar.documento("123")).isEqualTo("****");
        assertThat(Enmascarar.documento(null)).isEqualTo("****");
    }
}
