package com.atlas.platform.controller;

import com.atlas.platform.dto.ContratoDetalheResponse;
import com.atlas.platform.dto.ContratoRequest;
import com.atlas.platform.dto.ContratoResponse;
import com.atlas.platform.response.ApiResponse;
import com.atlas.platform.response.ApiResponses;
import com.atlas.platform.service.ContratoService;
import com.atlas.platform.util.PaginationUtils;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contratos")
public class ContratoController {

    private final ContratoService service;

    public ContratoController(ContratoService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<ContratoResponse> salvar(@RequestBody @Valid ContratoRequest request) {
        return ApiResponses.success("Contrato criado com sucesso", service.salvar(request));
    }

    @GetMapping
    public ApiResponse<?> listar(
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) String grupo,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        List<ContratoResponse> contratos = service.listar(ativo, grupo, nome);
        Object dados = PaginationUtils.requested(page, size)
                ? PaginationUtils.page(contratos, page, size)
                : contratos;
        return ApiResponses.success("Lista de contratos", dados);
    }

    @GetMapping("/{id}")
    public ApiResponse<ContratoResponse> buscarPorId(@PathVariable Long id) {
        return ApiResponses.success("Contrato encontrado com sucesso", service.buscarPorId(id));
    }

    @GetMapping("/{id}/detalhe")
    public ApiResponse<ContratoDetalheResponse> buscarDetalhePorId(@PathVariable Long id) {
        return ApiResponses.success("Detalhe do contrato carregado com sucesso", service.buscarDetalhePorId(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ContratoResponse> atualizar(@PathVariable Long id, @RequestBody @Valid ContratoRequest request) {
        return ApiResponses.success("Contrato atualizado com sucesso", service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletar(@PathVariable Long id) {
        service.deletar(id);

        return ApiResponses.successWithoutData("Contrato inativado com sucesso");
    }

    @PatchMapping("/{id}/ativar")
    public ApiResponse<ContratoResponse> ativar(@PathVariable Long id) {
        return ApiResponses.success("Contrato ativado com sucesso", service.ativar(id));
    }

    @PatchMapping("/{id}/inativar")
    public ApiResponse<ContratoResponse> inativar(@PathVariable Long id) {
        return ApiResponses.success("Contrato inativado com sucesso", service.inativar(id));
    }
}
