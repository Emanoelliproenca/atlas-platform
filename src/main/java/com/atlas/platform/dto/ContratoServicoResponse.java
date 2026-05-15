package com.atlas.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContratoServicoResponse {

    private Long id;
    private Long contratoId;
    private String contratoNome;
    private Long servicoId;
    private String servicoNome;
    private String versao;
    private String observacao;
    private String setor;
    private Boolean ativo;

}
