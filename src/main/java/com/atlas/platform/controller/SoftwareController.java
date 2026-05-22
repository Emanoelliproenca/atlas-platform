package com.atlas.platform.controller;

import com.atlas.platform.dto.ServicoRequest;
import com.atlas.platform.dto.ServicoResponse;
import com.atlas.platform.dto.SoftwareResumoResponse;
import com.atlas.platform.dto.SoftwareArtefatoResponse;
import com.atlas.platform.dto.SoftwareSyncResponse;
import com.atlas.platform.model.SoftwareArtefato;
import com.atlas.platform.response.ApiResponse;
import com.atlas.platform.response.ApiResponses;
import com.atlas.platform.service.SoftwareArtefatoService;
import com.atlas.platform.service.SoftwareConsultaService;
import com.atlas.platform.service.SoftwareSincronizacaoService;
import com.atlas.platform.service.ServicoService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/softwares")
public class SoftwareController {

    private final SoftwareConsultaService service;
    private final SoftwareArtefatoService artefatoService;
    private final SoftwareSincronizacaoService sincronizacaoService;
    private final ServicoService servicoService;

    public SoftwareController(
            SoftwareConsultaService service,
            SoftwareArtefatoService artefatoService,
            SoftwareSincronizacaoService sincronizacaoService,
            ServicoService servicoService
    ) {
        this.service = service;
        this.artefatoService = artefatoService;
        this.sincronizacaoService = sincronizacaoService;
        this.servicoService = servicoService;
    }

    @GetMapping
    public ApiResponse<List<SoftwareResumoResponse>> listar() {
        return ApiResponses.success("Lista de softwares carregada com sucesso", service.listar());
    }

    @PostMapping
    public ApiResponse<ServicoResponse> salvar(@RequestBody @Valid ServicoRequest request) {
        return ApiResponses.success("Software criado com sucesso", servicoService.salvar(request));
    }

    @PutMapping("/{servicoId}")
    public ApiResponse<ServicoResponse> atualizar(
            @PathVariable Long servicoId,
            @RequestBody @Valid ServicoRequest request
    ) {
        return ApiResponses.success("Software atualizado com sucesso", servicoService.atualizar(servicoId, request));
    }

    @PatchMapping("/{servicoId}/ativar")
    public ApiResponse<ServicoResponse> ativar(@PathVariable Long servicoId) {
        return ApiResponses.success("Software ativado com sucesso", servicoService.ativar(servicoId));
    }

    @PatchMapping("/{servicoId}/inativar")
    public ApiResponse<ServicoResponse> inativar(@PathVariable Long servicoId) {
        return ApiResponses.success("Software inativado com sucesso", servicoService.inativar(servicoId));
    }

    @PostMapping("/sincronizar")
    public ApiResponse<SoftwareSyncResponse> sincronizar() {
        return ApiResponses.success("Sincronização de versões concluída", sincronizacaoService.sincronizarManifestoExterno());
    }

    @GetMapping("/{servicoId}/artefatos")
    public ApiResponse<List<SoftwareArtefatoResponse>> listarArtefatos(@PathVariable Long servicoId) {
        return ApiResponses.success("Artefatos do sistema carregados com sucesso", artefatoService.listarPorServico(servicoId));
    }

    @PostMapping("/{servicoId}/artefatos")
    public ApiResponse<SoftwareArtefatoResponse> uploadArtefato(
            @PathVariable Long servicoId,
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(value = "versao", required = false) String versao
    ) {
        return ApiResponses.success("Artefato adicionado com sucesso", artefatoService.salvar(servicoId, arquivo, versao));
    }

    @GetMapping("/{servicoId}/download")
    public ResponseEntity<ByteArrayResource> baixarArtefato(@PathVariable Long servicoId) {
        SoftwareArtefato artefato = artefatoService.buscarArtefatoAtual(servicoId);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(artefato.getNome(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(artefato.getTipoConteudo()))
                .contentLength(artefato.getTamanho())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new ByteArrayResource(artefato.getConteudo()));
    }
}
