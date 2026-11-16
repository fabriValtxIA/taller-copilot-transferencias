package com.taller.transferencias;

/** Enmascara datos sensibles antes de escribirlos en logs: solo quedan visibles los últimos 4 caracteres. */
public final class Enmascarar {

    private static final int VISIBLES = 4;

    private Enmascarar() {
    }

    public static String cuenta(String numero) {
        return ocultar(numero);
    }

    public static String documento(String documento) {
        return ocultar(documento);
    }

    private static String ocultar(String valor) {
        if (valor == null || valor.length() <= VISIBLES) {
            return "****";
        }
        return "*".repeat(valor.length() - VISIBLES) + valor.substring(valor.length() - VISIBLES);
    }
}
