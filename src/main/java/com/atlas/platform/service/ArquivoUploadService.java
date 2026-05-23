package com.atlas.platform.service;

import com.atlas.platform.exception.BusinessException;
import java.io.IOException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ArquivoUploadService {

    private static final String TIPO_CONTEUDO_PADRAO = "application/octet-stream";

    public ArquivoUploadData preparar(MultipartFile arquivo, String nomePadrao, String mensagemArquivoVazio, String mensagemLeitura) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new BusinessException(mensagemArquivoVazio);
        }

        try {
            return new ArquivoUploadData(
                    normalizarNome(arquivo.getOriginalFilename(), nomePadrao),
                    normalizarTipoConteudo(arquivo.getContentType()),
                    arquivo.getSize(),
                    arquivo.getBytes()
            );
        } catch (IOException ex) {
            throw new BusinessException(mensagemLeitura);
        }
    }

    private String normalizarNome(String nome, String nomePadrao) {
        String nomeNormalizado = nome == null ? "" : nome.trim();
        if (nomeNormalizado.isBlank()) {
            return nomePadrao;
        }

        return nomeNormalizado.replaceAll("[\\\\/]+", "_");
    }

    private String normalizarTipoConteudo(String tipoConteudo) {
        return tipoConteudo == null || tipoConteudo.isBlank()
                ? TIPO_CONTEUDO_PADRAO
                : tipoConteudo;
    }
}
