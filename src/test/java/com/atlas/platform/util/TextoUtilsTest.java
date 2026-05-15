package com.atlas.platform.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TextoUtilsTest {

    @Test
    void normalizarObrigatorioDeveRemoverEspacosLaterais() {
        assertThat(TextoUtils.normalizarObrigatorio("  Portal Operacional  ")).isEqualTo("Portal Operacional");
    }

    @Test
    void normalizarOpcionalDeveRetornarNullQuandoValorFicarVazio() {
        assertThat(TextoUtils.normalizarOpcional("   ")).isNull();
    }

    @Test
    void normalizarFiltroDeveRetornarNullQuandoReceberNuloOuVazio() {
        assertThat(TextoUtils.normalizarFiltro(null)).isNull();
        assertThat(TextoUtils.normalizarFiltro("   ")).isNull();
        assertThat(TextoUtils.normalizarFiltro("  suporte  ")).isEqualTo("suporte");
    }
}
