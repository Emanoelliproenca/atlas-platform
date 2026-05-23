package com.atlas.platform.service;

import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ArquivoDownloadService {

    public ResponseEntity<ByteArrayResource> criarResposta(
            String nome,
            String tipoConteudo,
            Long tamanho,
            byte[] conteudo,
            boolean download
    ) {
        ContentDisposition disposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(nome, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(tipoConteudo))
                .contentLength(tamanho)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new ByteArrayResource(conteudo));
    }
}
