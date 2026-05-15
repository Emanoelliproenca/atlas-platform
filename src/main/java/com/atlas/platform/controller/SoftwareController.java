package com.atlas.platform.controller;

import com.atlas.platform.dto.SoftwareResumoResponse;
import com.atlas.platform.response.ApiResponse;
import com.atlas.platform.response.ApiResponses;
import com.atlas.platform.service.SoftwareConsultaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/softwares")
public class SoftwareController {

    private final SoftwareConsultaService service;

    public SoftwareController(SoftwareConsultaService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<SoftwareResumoResponse>> listar() {
        return ApiResponses.success("Lista de softwares carregada com sucesso", service.listar());
    }
}
