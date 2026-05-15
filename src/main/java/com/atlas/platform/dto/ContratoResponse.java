package com.atlas.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContratoResponse {

    private Long id;
    private String nome;
    private String grupo;
    private String centralUrl;
    private String particularidades;
    private String documentacao;
    private Boolean ativo;

}
