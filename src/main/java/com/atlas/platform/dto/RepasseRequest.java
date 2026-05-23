package com.atlas.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RepasseRequest {

    @NotBlank(message = "Título é obrigatório")
    private String titulo;

    @NotBlank(message = "Categoria é obrigatória")
    private String categoria;

    @NotBlank(message = "Descrição é obrigatória")
    private String conteudo;

    @NotBlank(message = "Prioridade é obrigatória")
    private String prioridade;

    private Boolean ativo = true;
    private Boolean fixado = false;
    private String anexoNome;
}
