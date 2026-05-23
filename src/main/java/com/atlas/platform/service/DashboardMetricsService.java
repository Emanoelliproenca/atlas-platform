package com.atlas.platform.service;

import com.atlas.platform.dto.DashboardMetricsResponse;
import com.atlas.platform.model.Contrato;
import com.atlas.platform.model.ContratoServico;
import com.atlas.platform.model.Repasse;
import com.atlas.platform.model.Servico;
import com.atlas.platform.repository.ContratoRepository;
import com.atlas.platform.repository.ContratoServicoRepository;
import com.atlas.platform.repository.RepasseRepository;
import com.atlas.platform.repository.ServicoRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardMetricsService {

    private final ContratoRepository contratoRepository;
    private final ServicoRepository servicoRepository;
    private final ContratoServicoRepository contratoServicoRepository;
    private final RepasseRepository repasseRepository;

    public DashboardMetricsService(
            ContratoRepository contratoRepository,
            ServicoRepository servicoRepository,
            ContratoServicoRepository contratoServicoRepository,
            RepasseRepository repasseRepository
    ) {
        this.contratoRepository = contratoRepository;
        this.servicoRepository = servicoRepository;
        this.contratoServicoRepository = contratoServicoRepository;
        this.repasseRepository = repasseRepository;
    }

    public DashboardMetricsResponse carregar() {
        List<Contrato> contratos = contratoRepository.findAll();
        List<Servico> servicos = servicoRepository.findAll();
        List<ContratoServico> relacionamentos = contratoServicoRepository.findAll();
        List<Repasse> repasses = repasseRepository.findByAtivoTrue();

        long contratosAtivos = contratos.stream().filter(this::ativo).count();
        long servicosAtivos = servicos.stream().filter(this::ativo).count();
        long relacionamentosAtivos = relacionamentos.stream().filter(this::ativo).count();
        Set<Long> contratosComServico = relacionamentos.stream()
                .filter(this::ativo)
                .map(relacionamento -> relacionamento.getContrato().getId())
                .collect(Collectors.toSet());

        return new DashboardMetricsResponse(
                contratos.size(),
                contratosAtivos,
                contratos.size() - contratosAtivos,
                servicos.size(),
                servicosAtivos,
                servicos.size() - servicosAtivos,
                relacionamentos.size(),
                relacionamentosAtivos,
                repasses.size(),
                servicosAtivos,
                servicos.stream().filter(servico -> ativo(servico) && !possuiTexto(servico.getVersaoReferencia())).count(),
                percentual(contratos.stream().filter(contrato -> possuiTexto(contrato.getDocumentacao())).count(), contratos.size()),
                percentual(contratosComServico.size(), contratos.size()),
                contratosPorGrupo(contratos),
                servicosPorSetor(relacionamentos),
                ultimosContratos(contratos),
                ultimosRepasses(repasses)
        );
    }

    private List<DashboardMetricsResponse.DistribuicaoItem> contratosPorGrupo(List<Contrato> contratos) {
        return contratos.stream()
                .collect(Collectors.groupingBy(contrato -> label(contrato.getGrupo(), "Sem grupo"), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .map(entry -> new DashboardMetricsResponse.DistribuicaoItem(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<DashboardMetricsResponse.DistribuicaoItem> servicosPorSetor(List<ContratoServico> relacionamentos) {
        return relacionamentos.stream()
                .filter(this::ativo)
                .collect(Collectors.groupingBy(relacionamento -> label(relacionamento.getSetor(), "Sem setor"), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .map(entry -> new DashboardMetricsResponse.DistribuicaoItem(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<DashboardMetricsResponse.ContratoRecente> ultimosContratos(List<Contrato> contratos) {
        return contratos.stream()
                .sorted(Comparator.comparing(Contrato::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(contrato -> new DashboardMetricsResponse.ContratoRecente(
                        contrato.getId(),
                        contrato.getNome(),
                        contrato.getGrupo(),
                        contrato.getAtivo()
                ))
                .toList();
    }

    private List<DashboardMetricsResponse.RepasseRecente> ultimosRepasses(List<Repasse> repasses) {
        return repasses.stream()
                .sorted(Comparator.comparing(Repasse::getCriadoEm, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(repasse -> new DashboardMetricsResponse.RepasseRecente(
                        repasse.getId(),
                        repasse.getTitulo(),
                        repasse.getCategoria(),
                        repasse.getPrioridade(),
                        repasse.getCriadoEm()
                ))
                .toList();
    }

    private boolean ativo(Contrato contrato) {
        return Boolean.TRUE.equals(contrato.getAtivo());
    }

    private boolean ativo(Servico servico) {
        return Boolean.TRUE.equals(servico.getAtivo());
    }

    private boolean ativo(ContratoServico relacionamento) {
        return Boolean.TRUE.equals(relacionamento.getAtivo());
    }

    private boolean possuiTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    private String label(String valor, String fallback) {
        return possuiTexto(valor) ? valor.trim() : fallback;
    }

    private double percentual(long parte, long total) {
        if (total == 0) {
            return 0;
        }
        return Math.round((parte * 10000.0) / total) / 100.0;
    }
}
