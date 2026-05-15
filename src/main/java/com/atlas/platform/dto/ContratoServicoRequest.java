package com.atlas.platform.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ContratoServicoRequest {

    @NotNull(message = "Contrato é obrigatório")
    @Positive(message = "Contrato é obrigatório")
    private Long contratoId;

    @NotNull(message = "Serviço é obrigatório")
    @Positive(message = "Serviço é obrigatório")
    private Long servicoId;

    private String versao;
    private String observacao;
    private String setor;
    private Boolean ativo = true;

}
