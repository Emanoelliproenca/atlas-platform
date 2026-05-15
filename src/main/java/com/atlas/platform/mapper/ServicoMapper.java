package com.atlas.platform.mapper;

import com.atlas.platform.dto.ServicoRequest;
import com.atlas.platform.dto.ServicoResponse;
import com.atlas.platform.model.Servico;
import com.atlas.platform.util.TextoUtils;
import org.springframework.stereotype.Component;

@Component
public class ServicoMapper {

    public void aplicarRequest(Servico servico, ServicoRequest request) {
        servico.setNome(TextoUtils.normalizarObrigatorio(request.getNome()));
        servico.setDescricaoSoftware(TextoUtils.normalizarOpcional(request.getDescricaoSoftware()));
        servico.setAtivo(request.getAtivo());
    }

    public ServicoResponse toResponse(Servico servico) {
        return new ServicoResponse(
                servico.getId(),
                servico.getNome(),
                servico.getDescricaoSoftware(),
                servico.getAtivo()
        );
    }
}
