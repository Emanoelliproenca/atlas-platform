package com.atlas.platform.controller;

import com.atlas.platform.dto.ImportacaoPlanilhaResponse;
import com.atlas.platform.response.ApiResponse;
import com.atlas.platform.response.ApiResponses;
import com.atlas.platform.service.ImportacaoPlanilhaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/importacoes")
public class ImportacaoController {

    private final ImportacaoPlanilhaService service;

    public ImportacaoController(ImportacaoPlanilhaService service) {
        this.service = service;
    }

    @PostMapping("/planilha")
    public ApiResponse<ImportacaoPlanilhaResponse> importarPlanilha(@RequestParam("arquivo") MultipartFile arquivo) {
        return ApiResponses.success("Planilha importada com sucesso", service.importar(arquivo));
    }
}
