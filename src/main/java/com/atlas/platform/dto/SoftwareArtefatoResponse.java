package com.atlas.platform.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SoftwareArtefatoResponse {

    private Long id;
    private String nome;
    private String tipoConteudo;
    private Long tamanho;
    private String versao;
    private Boolean ativo;
    private LocalDateTime criadoEm;
}
