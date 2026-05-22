package com.atlas.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SoftwareResumoResponse {

    private Long id;
    private String nome;
    private String descricaoSoftware;
    private String versaoReferencia;
    private String linkSoftware;
    private int totalContratos;
    private Boolean ativo;
    private Boolean downloadDisponivel;
    private String artefatoNome;

}
