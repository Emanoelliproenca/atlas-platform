package com.atlas.platform.util;

public final class TextoUtils {

    private TextoUtils() {
    }

    public static String normalizarObrigatorio(String valor) {
        return valor == null ? null : valor.trim();
    }

    public static String normalizarOpcional(String valor) {
        return normalizarFiltro(valor);
    }

    public static String normalizarFiltro(String valor) {
        if (valor == null) {
            return null;
        }

        String normalizado = valor.trim();
        return normalizado.isEmpty() ? null : normalizado;
    }
}
