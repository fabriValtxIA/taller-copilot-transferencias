package com.taller.transferencias;

public final class Enmascarar {

    private Enmascarar() {
    }

    public static String cuenta(String cuenta) {
        return ultimosCuatro(cuenta);
    }

    public static String documento(String documento) {
        return ultimosCuatro(documento);
    }

    private static String ultimosCuatro(String valor) {
        if (valor == null || valor.isEmpty()) {
            return "****";
        }
        int inicio = Math.max(0, valor.length() - 4);
        return "*".repeat(inicio) + valor.substring(inicio);
    }
}
