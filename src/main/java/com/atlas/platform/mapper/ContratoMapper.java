package com.atlas.platform.mapper;

import com.atlas.platform.dto.ContratoRequest;
import com.atlas.platform.dto.ContratoResponse;
import com.atlas.platform.model.Contrato;
import com.atlas.platform.util.TextoUtils;
import org.springframework.stereotype.Component;

@Component
public class ContratoMapper {

    public void aplicarRequest(Contrato contrato, ContratoRequest request) {
        contrato.setNome(TextoUtils.normalizarObrigatorio(request.getNome()));
        contrato.setGrupo(TextoUtils.normalizarObrigatorio(request.getGrupo()));
        contrato.setCentralUrl(TextoUtils.normalizarObrigatorio(request.getCentralUrl()));
        contrato.setParticularidades(TextoUtils.normalizarOpcional(request.getParticularidades()));
        contrato.setDocumentacao(TextoUtils.normalizarOpcional(request.getDocumentacao()));
        contrato.setAtivo(request.getAtivo());
    }

    public ContratoResponse toResponse(Contrato contrato) {
        return new ContratoResponse(
                contrato.getId(),
                contrato.getNome(),
                contrato.getGrupo(),
                contrato.getCentralUrl(),
                contrato.getParticularidades(),
                contrato.getDocumentacao(),
                contrato.getAtivo()
        );
    }
}
