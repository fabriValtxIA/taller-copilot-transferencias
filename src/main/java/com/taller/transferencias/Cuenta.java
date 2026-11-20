package com.taller.transferencias;

import java.math.BigDecimal;

public record Cuenta(String numero, String documento, BigDecimal saldo) {
}
