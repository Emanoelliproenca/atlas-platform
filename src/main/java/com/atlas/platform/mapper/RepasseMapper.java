package com.atlas.platform.mapper;

import com.atlas.platform.dto.RepasseResponse;
import com.atlas.platform.model.Repasse;
import org.springframework.stereotype.Component;

@Component
public class RepasseMapper {

    public RepasseResponse toResponse(Repasse repasse) {
        return new RepasseResponse(
                repasse.getId(),
                repasse.getTitulo(),
                repasse.getCategoria(),
                repasse.getConteudo(),
                repasse.getPrioridade(),
                repasse.getAtivo(),
                repasse.getFixado(),
                repasse.getAutor(),
                repasse.getAnexoNome(),
                repasse.getCriadoEm()
        );
    }
}
