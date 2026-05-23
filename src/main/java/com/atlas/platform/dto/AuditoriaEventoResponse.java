package com.atlas.platform.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuditoriaEventoResponse {

    private Long id;
    private String entidade;
    private Long entidadeId;
    private String acao;
    private String usuario;
    private String detalhes;
    private String ip;
    private Instant timestamp;
}
