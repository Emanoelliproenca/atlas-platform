package com.atlas.platform.controller;

import com.atlas.platform.dto.AuditoriaEventoResponse;
import com.atlas.platform.response.ApiResponse;
import com.atlas.platform.response.ApiResponses;
import com.atlas.platform.service.AuditoriaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auditoria")
public class AuditoriaController {

    private final AuditoriaService service;

    public AuditoriaController(AuditoriaService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<AuditoriaEventoResponse>> listarRecentes() {
        return ApiResponses.success("Histórico de auditoria carregado com sucesso", service.listarRecentes());
    }
}
