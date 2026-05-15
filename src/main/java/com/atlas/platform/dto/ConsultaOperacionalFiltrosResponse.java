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
public class ConsultaOperacionalFiltrosResponse {

    private List<ContratoResponse> contratos;
    private List<ServicoResponse> servicos;
    private List<String> grupos;
    private List<String> setores;

}
