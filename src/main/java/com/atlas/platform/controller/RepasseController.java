package com.atlas.platform.controller;

import com.atlas.platform.dto.RepasseRequest;
import com.atlas.platform.dto.RepasseResponse;
import com.atlas.platform.response.ApiResponse;
import com.atlas.platform.response.ApiResponses;
import com.atlas.platform.service.RepasseService;
import com.atlas.platform.util.PaginationUtils;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/repasses")
public class RepasseController {

    private final RepasseService service;

    public RepasseController(RepasseService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<?> listar(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        List<RepasseResponse> repasses = service.listarAtivos();
        Object dados = PaginationUtils.requested(page, size)
                ? PaginationUtils.page(repasses, page, size)
                : repasses;
        return ApiResponses.success("Lista de repasses carregada com sucesso", dados);
    }

    @PostMapping
    public ApiResponse<RepasseResponse> salvar(
            @RequestBody @Valid RepasseRequest request,
            Authentication authentication
    ) {
        return ApiResponses.success("Repasse criado com sucesso", service.salvar(request, authentication));
    }

    @PutMapping("/{id}")
    public ApiResponse<RepasseResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid RepasseRequest request,
            Authentication authentication
    ) {
        return ApiResponses.success("Repasse atualizado com sucesso", service.atualizar(id, request, authentication));
    }

    @PatchMapping("/{id}/ativar")
    public ApiResponse<RepasseResponse> ativar(@PathVariable Long id) {
        return ApiResponses.success("Repasse ativado com sucesso", service.ativar(id));
    }

    @PatchMapping("/{id}/inativar")
    public ApiResponse<RepasseResponse> inativar(@PathVariable Long id) {
        return ApiResponses.success("Repasse inativado com sucesso", service.inativar(id));
    }

    @PatchMapping("/{id}/fixar")
    public ApiResponse<RepasseResponse> fixar(@PathVariable Long id) {
        return ApiResponses.success("Destaque do repasse atualizado com sucesso", service.alternarFixado(id));
    }
}
