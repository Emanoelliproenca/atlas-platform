package com.atlas.platform.controller;

import com.atlas.platform.dto.ConsultaOperacionalResponse;
import com.atlas.platform.response.ApiResponse;
import com.atlas.platform.response.ApiResponses;
import com.atlas.platform.service.PainelOperacionalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/painel/operacional")
public class PainelOperacionalController {

    private final PainelOperacionalService service;

    public PainelOperacionalController(PainelOperacionalService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<ConsultaOperacionalResponse> consultar(
            @RequestParam(required = false) List<Long> contratoIds,
            @RequestParam(required = false) List<Long> servicoIds,
            @RequestParam(required = false) String grupo,
            @RequestParam(required = false) String setor,
            @RequestParam(required = false) String contratoNome,
            @RequestParam(required = false) String servicoNome
    ) {
        return ApiResponses.success(
                "Consulta operacional carregada com sucesso",
                service.consultar(contratoIds, servicoIds, grupo, setor, contratoNome, servicoNome)
        );
    }
}
