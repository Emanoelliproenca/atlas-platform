package com.atlas.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaOperacionalLinhaResponse {

    private Long relacionamentoId;
    private Long contratoId;
    private String contratoNome;
    private String grupo;
    private String centralUrl;
    private Long servicoId;
    private String servicoNome;
    private String versao;
    private String observacao;
    private String setor;

}
