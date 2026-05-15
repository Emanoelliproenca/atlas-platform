package com.atlas.platform.controller;

import com.atlas.platform.dto.ContratoArquivoResponse;
import com.atlas.platform.model.ContratoArquivo;
import com.atlas.platform.response.ApiResponse;
import com.atlas.platform.response.ApiResponses;
import com.atlas.platform.service.ContratoArquivoService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/contratos/{contratoId}/arquivos")
public class ContratoArquivoController {

    private final ContratoArquivoService service;

    public ContratoArquivoController(ContratoArquivoService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<ContratoArquivoResponse>> listar(@PathVariable Long contratoId) {
        return ApiResponses.success("Arquivos do contrato carregados com sucesso", service.listarPorContrato(contratoId));
    }

    @PostMapping
    public ApiResponse<ContratoArquivoResponse> upload(
            @PathVariable Long contratoId,
            @RequestParam("arquivo") MultipartFile arquivo
    ) {
        return ApiResponses.success("Arquivo adicionado com sucesso", service.salvar(contratoId, arquivo));
    }

    @GetMapping("/{arquivoId}/abrir")
    public ResponseEntity<ByteArrayResource> abrir(@PathVariable Long contratoId, @PathVariable Long arquivoId) {
        return criarRespostaArquivo(service.buscarArquivo(contratoId, arquivoId), false);
    }

    @GetMapping("/{arquivoId}/baixar")
    public ResponseEntity<ByteArrayResource> baixar(@PathVariable Long contratoId, @PathVariable Long arquivoId) {
        return criarRespostaArquivo(service.buscarArquivo(contratoId, arquivoId), true);
    }

    private ResponseEntity<ByteArrayResource> criarRespostaArquivo(ContratoArquivo arquivo, boolean download) {
        ContentDisposition disposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(arquivo.getNome(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(arquivo.getTipoConteudo()))
                .contentLength(arquivo.getTamanho())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new ByteArrayResource(arquivo.getConteudo()));
    }
}
