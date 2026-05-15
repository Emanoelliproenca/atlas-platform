package com.atlas.platform.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaOperacionalResponse {

    private ConsultaOperacionalResumoResponse resumo;
    private ConsultaOperacionalFiltrosResponse filtros;
    private List<ConsultaOperacionalLinhaResponse> linhas;

}
