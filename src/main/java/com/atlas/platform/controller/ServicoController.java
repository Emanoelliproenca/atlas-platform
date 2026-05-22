package com.atlas.platform.controller;

import com.atlas.platform.dto.ServicoDetalheResponse;
import com.atlas.platform.dto.ServicoRequest;
import com.atlas.platform.dto.ServicoResponse;
import com.atlas.platform.response.ApiResponse;
import com.atlas.platform.response.ApiResponses;
import com.atlas.platform.service.ServicoService;
import com.atlas.platform.util.PaginationUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoService service;

    public ServicoController(ServicoService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<ServicoResponse> salvar(@RequestBody @Valid ServicoRequest request) {
        return ApiResponses.success("Serviço criado com sucesso", service.salvar(request));
    }

    @GetMapping
    public ApiResponse<?> listar(
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        List<ServicoResponse> servicos = service.listar(ativo, nome);
        Object dados = PaginationUtils.requested(page, size)
                ? PaginationUtils.page(servicos, page, size)
                : servicos;
        return ApiResponses.success("Lista de serviços", dados);
    }

    @GetMapping("/{id}")
    public ApiResponse<ServicoResponse> buscarPorId(@PathVariable Long id) {
        return ApiResponses.success("Serviço encontrado com sucesso", service.buscarPorId(id));
    }

    @GetMapping("/{id}/detalhe")
    public ApiResponse<ServicoDetalheResponse> buscarDetalhePorId(@PathVariable Long id) {
        return ApiResponses.success("Detalhe do serviço carregado com sucesso", service.buscarDetalhePorId(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ServicoResponse> atualizar(@PathVariable Long id, @RequestBody @Valid ServicoRequest request) {
        return ApiResponses.success("Serviço atualizado com sucesso", service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ApiResponses.successWithoutData("Serviço inativado com sucesso");
    }

    @PatchMapping("/{id}/ativar")
    public ApiResponse<ServicoResponse> ativar(@PathVariable Long id) {
        return ApiResponses.success("Serviço ativado com sucesso", service.ativar(id));
    }

    @PatchMapping("/{id}/inativar")
    public ApiResponse<ServicoResponse> inativar(@PathVariable Long id) {
        return ApiResponses.success("Serviço inativado com sucesso", service.inativar(id));
    }
}
