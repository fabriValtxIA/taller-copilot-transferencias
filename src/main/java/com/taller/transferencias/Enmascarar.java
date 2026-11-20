package com.taller.transferencias;

public final class Enmascarar {

    private Enmascarar() {
    }

    public static String cuenta(String cuenta) {
        if (cuenta == null || cuenta.isBlank()) {
            return "****";
        }
        int visibles = Math.min(4, cuenta.length());
        return "*".repeat(cuenta.length() - visibles) + cuenta.substring(cuenta.length() - visibles);
    }

    public static String documento(String documento) {
        if (documento == null || documento.isBlank()) {
            return "****";
        }
        int visibles = Math.min(4, documento.length());
        return "*".repeat(documento.length() - visibles) + documento.substring(documento.length() - visibles);
    }
}
