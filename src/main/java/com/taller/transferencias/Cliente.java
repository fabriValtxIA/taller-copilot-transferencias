package com.taller.transferencias;

import java.math.BigDecimal;

public record Cliente(String documento, String nombre, BigDecimal limiteDiario) {
}
