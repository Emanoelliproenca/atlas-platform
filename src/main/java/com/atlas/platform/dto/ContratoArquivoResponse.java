package com.atlas.platform.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContratoArquivoResponse {

    private Long id;
    private String nome;
    private String tipoConteudo;
    private Long tamanho;
    private LocalDateTime criadoEm;
}
