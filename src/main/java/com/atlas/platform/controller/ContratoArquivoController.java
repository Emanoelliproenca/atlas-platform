package com.atlas.platform.controller;

import com.atlas.platform.dto.ContratoArquivoResponse;
import com.atlas.platform.model.ContratoArquivo;
import com.atlas.platform.response.ApiResponse;
import com.atlas.platform.response.ApiResponses;
import com.atlas.platform.service.ArquivoDownloadService;
import com.atlas.platform.service.ContratoArquivoService;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/contratos/{contratoId}/arquivos")
public class ContratoArquivoController {

    private final ContratoArquivoService service;
    private final ArquivoDownloadService arquivoDownloadService;

    public ContratoArquivoController(ContratoArquivoService service, ArquivoDownloadService arquivoDownloadService) {
        this.service = service;
        this.arquivoDownloadService = arquivoDownloadService;
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
        return arquivoDownloadService.criarResposta(
                arquivo.getNome(),
                arquivo.getTipoConteudo(),
                arquivo.getTamanho(),
                arquivo.getConteudo(),
                download
        );
    }
}
