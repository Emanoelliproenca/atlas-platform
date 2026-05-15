package com.atlas.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ContratoRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "Grupo é obrigatório")
    private String grupo;

    @NotBlank(message = "Central URL é obrigatória")
    private String centralUrl;

    private String particularidades;

    private String documentacao;

    @NotNull(message = "Ativo é obrigatório")
    private Boolean ativo;

}
