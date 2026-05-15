package com.atlas.platform.controller;

import com.atlas.platform.dto.RepasseResponse;
import com.atlas.platform.response.ApiResponse;
import com.atlas.platform.response.ApiResponses;
import com.atlas.platform.service.RepasseService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/repasses")
public class RepasseController {

    private final RepasseService service;

    public RepasseController(RepasseService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<RepasseResponse>> listar() {
        return ApiResponses.success("Lista de repasses carregada com sucesso", service.listarAtivos());
    }
}
