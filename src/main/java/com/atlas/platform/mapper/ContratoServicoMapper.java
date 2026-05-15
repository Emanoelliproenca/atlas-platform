package com.atlas.platform.mapper;

import com.atlas.platform.dto.ContratoServicoRequest;
import com.atlas.platform.dto.ContratoServicoResponse;
import com.atlas.platform.model.ContratoServico;
import com.atlas.platform.util.TextoUtils;
import org.springframework.stereotype.Component;

@Component
public class ContratoServicoMapper {

    public void aplicarCamposEditaveis(ContratoServico relacionamento, ContratoServicoRequest request) {
        relacionamento.setAtivo(request.getAtivo() == null || request.getAtivo());
        relacionamento.setVersao(TextoUtils.normalizarOpcional(request.getVersao()));
        relacionamento.setObservacao(TextoUtils.normalizarOpcional(request.getObservacao()));
        relacionamento.setSetor(TextoUtils.normalizarOpcional(request.getSetor()));
    }

    public ContratoServicoResponse toResponse(ContratoServico relacionamento) {
        return new ContratoServicoResponse(
                relacionamento.getId(),
                relacionamento.getContrato().getId(),
                relacionamento.getContrato().getNome(),
                relacionamento.getServico().getId(),
                relacionamento.getServico().getNome(),
                relacionamento.getVersao(),
                relacionamento.getObservacao(),
                relacionamento.getSetor(),
                relacionamento.getAtivo()
        );
    }
}
