package com.atlas.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicoResponse {

    private Long id;
    private String nome;
    private String descricaoSoftware;
    private Boolean ativo;

}
