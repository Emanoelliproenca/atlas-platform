package com.atlas.platform.service;

public record ArquivoUploadData(
        String nome,
        String tipoConteudo,
        Long tamanho,
        byte[] conteudo
) {
}
