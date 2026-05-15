package com.atlas.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RepasseResponse {

    private Long id;
    private String titulo;
    private String categoria;
    private String conteudo;
    private String prioridade;
    private Boolean ativo;

}
