package com.atlas.platform.service;

import com.atlas.platform.dto.SoftwareResumoResponse;
import com.atlas.platform.model.ContratoServico;
import com.atlas.platform.model.Servico;
import com.atlas.platform.repository.ContratoServicoRepository;
import com.atlas.platform.repository.ServicoRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SoftwareConsultaService {

    private static final Comparator<Servico> ORDENACAO_SOFTWARES =
            Comparator.comparing(Servico::getNome, String.CASE_INSENSITIVE_ORDER);

    private final ServicoRepository servicoRepository;
    private final ContratoServicoRepository contratoServicoRepository;

    public SoftwareConsultaService(ServicoRepository servicoRepository, ContratoServicoRepository contratoServicoRepository) {
        this.servicoRepository = servicoRepository;
        this.contratoServicoRepository = contratoServicoRepository;
    }

    public List<SoftwareResumoResponse> listar() {
        Map<Long, List<ContratoServico>> relacionamentosPorServico = agruparRelacionamentosPorServico();

        return listarServicosOrdenados().stream()
                .map(servico -> toResponse(servico, relacionamentosPorServico))
                .toList();
    }

    private List<Servico> listarServicosOrdenados() {
        return servicoRepository.findAll().stream()
                .sorted(ORDENACAO_SOFTWARES)
                .toList();
    }

    private Map<Long, List<ContratoServico>> agruparRelacionamentosPorServico() {
        return contratoServicoRepository.findAll().stream()
                .collect(Collectors.groupingBy(relacionamento -> relacionamento.getServico().getId()));
    }

    private SoftwareResumoResponse toResponse(Servico servico, Map<Long, List<ContratoServico>> relacionamentosPorServico) {
        List<ContratoServico> relacionamentos = relacionamentosPorServico.getOrDefault(servico.getId(), List.of());

        return new SoftwareResumoResponse(
                servico.getId(),
                servico.getNome(),
                servico.getDescricaoSoftware(),
                extrairVersaoReferencia(relacionamentos),
                relacionamentos.size(),
                servico.getAtivo()
        );
    }

    private String extrairVersaoReferencia(List<ContratoServico> relacionamentos) {
        return relacionamentos.stream()
                .map(ContratoServico::getVersao)
                .filter(this::possuiTexto)
                .findFirst()
                .orElse("");
    }

    private boolean possuiTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
