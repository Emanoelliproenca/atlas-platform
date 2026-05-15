package com.atlas.platform.service;

import com.atlas.platform.dto.ConsultaOperacionalFiltrosResponse;
import com.atlas.platform.dto.ConsultaOperacionalLinhaResponse;
import com.atlas.platform.dto.ConsultaOperacionalResponse;
import com.atlas.platform.dto.ConsultaOperacionalResumoResponse;
import com.atlas.platform.dto.ContratoResponse;
import com.atlas.platform.dto.ContratoServicoResponse;
import com.atlas.platform.dto.ServicoResponse;
import com.atlas.platform.mapper.PainelOperacionalMapper;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PainelOperacionalService {

    private final ContratoService contratoService;
    private final ServicoService servicoService;
    private final ContratoServicoService contratoServicoService;
    private final PainelOperacionalMapper painelOperacionalMapper;

    public PainelOperacionalService(
            ContratoService contratoService,
            ServicoService servicoService,
            ContratoServicoService contratoServicoService,
            PainelOperacionalMapper painelOperacionalMapper
    ) {
        this.contratoService = contratoService;
        this.servicoService = servicoService;
        this.contratoServicoService = contratoServicoService;
        this.painelOperacionalMapper = painelOperacionalMapper;
    }

    public ConsultaOperacionalResponse consultar(
            List<Long> contratoIds,
            List<Long> servicoIds,
            String grupo,
            String setor,
            String contratoNome,
            String servicoNome
    ) {
        List<ContratoServicoResponse> relacionamentos = contratoServicoService
                .listar(contratoIds, servicoIds, grupo, setor, contratoNome, servicoNome);
        relacionamentos = relacionamentos.stream()
                .filter(item -> Boolean.TRUE.equals(item.getAtivo()))
                .toList();

        List<ContratoResponse> contratos = contratoService.listar(true, null, null).stream()
                .sorted(Comparator.comparing(ContratoResponse::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<ServicoResponse> servicos = servicoService.listar(true, null).stream()
                .sorted(Comparator.comparing(ServicoResponse::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();

        Map<Long, ContratoResponse> contratosPorId = contratos.stream()
                .collect(java.util.stream.Collectors.toMap(ContratoResponse::getId, Function.identity()));

        List<ConsultaOperacionalLinhaResponse> linhas = painelOperacionalMapper.criarLinhas(relacionamentos, contratosPorId);
        List<String> grupos = painelOperacionalMapper.extrairGrupos(contratos);
        List<String> setores = painelOperacionalMapper.extrairSetores(linhas);
        ConsultaOperacionalResumoResponse resumo = painelOperacionalMapper.criarResumo(linhas);
        ConsultaOperacionalFiltrosResponse filtros = painelOperacionalMapper.criarFiltros(contratos, servicos, grupos, setores);

        return new ConsultaOperacionalResponse(resumo, filtros, linhas);
    }
}
