package com.taller.transferencias;

public final class Enmascarar {

    private Enmascarar() {
    }

    public static String cuenta(String cuenta) {
        return ultimosCaracteres(cuenta);
    }

    public static String documento(String documento) {
        return ultimosCaracteres(documento);
    }

    private static String ultimosCaracteres(String valor) {
        if (valor == null || valor.isEmpty()) {
            return "****";
        }
        int visibles = Math.min(4, valor.length());
        return "*".repeat(valor.length() - visibles) + valor.substring(valor.length() - visibles);
    }
}
