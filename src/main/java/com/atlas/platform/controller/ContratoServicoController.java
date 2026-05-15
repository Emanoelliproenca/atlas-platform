package com.atlas.platform.controller;

import com.atlas.platform.dto.ContratoServicoRequest;
import com.atlas.platform.dto.ContratoServicoResponse;
import com.atlas.platform.response.ApiResponse;
import com.atlas.platform.response.ApiResponses;
import com.atlas.platform.service.ContratoServicoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;

import java.util.List;

@RestController
@RequestMapping("/contrato-servicos")
public class ContratoServicoController {

    private final ContratoServicoService service;

    public ContratoServicoController(ContratoServicoService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<ContratoServicoResponse> salvar(@RequestBody @Valid ContratoServicoRequest request) {
        return ApiResponses.success("Relacionamento criado com sucesso", service.salvar(request));
    }

    @GetMapping
    public ApiResponse<List<ContratoServicoResponse>> listar(
            @RequestParam(required = false) List<Long> contratoIds,
            @RequestParam(required = false) List<Long> servicoIds,
            @RequestParam(required = false) String grupo,
            @RequestParam(required = false) String setor,
            @RequestParam(required = false) String contratoNome,
            @RequestParam(required = false) String servicoNome
    ) {
        return ApiResponses.success(
                "Lista de relacionamentos",
                service.listar(contratoIds, servicoIds, grupo, setor, contratoNome, servicoNome)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<ContratoServicoResponse> buscarPorId(@PathVariable Long id) {
        return ApiResponses.success("Relacionamento encontrado com sucesso", service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ContratoServicoResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid ContratoServicoRequest request
    ) {
        return ApiResponses.success("Relacionamento atualizado com sucesso", service.atualizar(id, request));
    }

    @PatchMapping("/{id}/ativar")
    public ApiResponse<ContratoServicoResponse> ativar(@PathVariable Long id) {
        return ApiResponses.success("Relacionamento ativado com sucesso", service.ativar(id));
    }

    @PatchMapping("/{id}/inativar")
    public ApiResponse<ContratoServicoResponse> inativar(@PathVariable Long id) {
        return ApiResponses.success("Relacionamento inativado com sucesso", service.inativar(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ApiResponses.successWithoutData("Relacionamento deletado com sucesso");
    }
}
