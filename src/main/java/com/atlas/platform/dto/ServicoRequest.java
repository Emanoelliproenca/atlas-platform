package com.atlas.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ServicoRequest {

    @NotBlank(message = "Nome do serviço é obrigatório")
    private String nome;

    private String descricaoSoftware;

    private String versaoReferencia;

    private String linkSoftware;

    @NotNull(message = "Ativo é obrigatório")
    private Boolean ativo;

}
