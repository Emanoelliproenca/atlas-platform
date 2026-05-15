package com.atlas.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaOperacionalResumoResponse {

    private long totalRelacionamentos;
    private long totalContratos;
    private long totalServicos;
    private long totalGrupos;
    private long totalSetores;

}
