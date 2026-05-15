package com.atlas.platform.mapper;

import com.atlas.platform.dto.ConsultaOperacionalFiltrosResponse;
import com.atlas.platform.dto.ConsultaOperacionalLinhaResponse;
import com.atlas.platform.dto.ConsultaOperacionalResumoResponse;
import com.atlas.platform.dto.ContratoResponse;
import com.atlas.platform.dto.ContratoServicoResponse;
import com.atlas.platform.dto.ServicoResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PainelOperacionalMapper {

    public List<ConsultaOperacionalLinhaResponse> criarLinhas(
            List<ContratoServicoResponse> relacionamentos,
            Map<Long, ContratoResponse> contratosPorId
    ) {
        return relacionamentos.stream()
                .map(item -> toLinha(item, contratosPorId.get(item.getContratoId())))
                .sorted(Comparator
                        .comparing(ConsultaOperacionalLinhaResponse::getContratoNome, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ConsultaOperacionalLinhaResponse::getServicoNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<String> extrairGrupos(List<ContratoResponse> contratos) {
        return contratos.stream()
                .map(ContratoResponse::getGrupo)
                .filter(this::possuiTexto)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<String> extrairSetores(List<ConsultaOperacionalLinhaResponse> linhas) {
        return linhas.stream()
                .map(ConsultaOperacionalLinhaResponse::getSetor)
                .filter(this::possuiTexto)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public ConsultaOperacionalResumoResponse criarResumo(List<ConsultaOperacionalLinhaResponse> linhas) {
        return new ConsultaOperacionalResumoResponse(
                linhas.size(),
                linhas.stream().map(ConsultaOperacionalLinhaResponse::getContratoId).distinct().count(),
                linhas.stream().map(ConsultaOperacionalLinhaResponse::getServicoId).distinct().count(),
                linhas.stream().map(ConsultaOperacionalLinhaResponse::getGrupo).filter(this::possuiTexto).distinct().count(),
                linhas.stream().map(ConsultaOperacionalLinhaResponse::getSetor).filter(this::possuiTexto).distinct().count()
        );
    }

    public ConsultaOperacionalFiltrosResponse criarFiltros(
            List<ContratoResponse> contratos,
            List<ServicoResponse> servicos,
            List<String> grupos,
            List<String> setores
    ) {
        return new ConsultaOperacionalFiltrosResponse(contratos, servicos, grupos, setores);
    }

    private ConsultaOperacionalLinhaResponse toLinha(ContratoServicoResponse item, ContratoResponse contrato) {
        return new ConsultaOperacionalLinhaResponse(
                item.getId(),
                item.getContratoId(),
                item.getContratoNome(),
                contrato != null ? contrato.getGrupo() : null,
                contrato != null ? contrato.getCentralUrl() : null,
                item.getServicoId(),
                item.getServicoNome(),
                item.getVersao(),
                item.getObservacao(),
                item.getSetor()
        );
    }

    private boolean possuiTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
