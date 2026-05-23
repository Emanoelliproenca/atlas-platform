package com.atlas.platform.controller;

import com.atlas.platform.dto.DashboardMetricsResponse;
import com.atlas.platform.response.ApiResponse;
import com.atlas.platform.response.ApiResponses;
import com.atlas.platform.service.DashboardMetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardMetricsService service;

    public DashboardController(DashboardMetricsService service) {
        this.service = service;
    }

    @GetMapping("/metrics")
    public ApiResponse<DashboardMetricsResponse> metrics() {
        return ApiResponses.success("Métricas do dashboard carregadas com sucesso", service.carregar());
    }
}
