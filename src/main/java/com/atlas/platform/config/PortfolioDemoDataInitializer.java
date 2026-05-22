package com.atlas.platform.config;

import com.atlas.platform.model.Contrato;
import com.atlas.platform.model.ContratoServico;
import com.atlas.platform.model.Servico;
import com.atlas.platform.repository.ContratoRepository;
import com.atlas.platform.repository.ContratoServicoRepository;
import com.atlas.platform.repository.ServicoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"demo", "seed"})
public class PortfolioDemoDataInitializer {

    @Bean
    public CommandLineRunner inicializarDadosPortfolio(
            ContratoRepository contratoRepository,
            ServicoRepository servicoRepository,
            ContratoServicoRepository contratoServicoRepository
    ) {
        return args -> {
            Contrato aurora = obterOuCriarContrato(contratoRepository,
                    "Atlas Labs Retail",
                    "Varejo",
                    "https://atlas-labs-retail.example.invalid",
                    "Operação fictícia de varejo com múltiplas unidades.",
                    "Documentação operacional demonstrativa para consulta do time."
            );
            Contrato lumen = obterOuCriarContrato(contratoRepository,
                    "Atlas Labs Health",
                    "Saúde",
                    "https://atlas-labs-health.example.invalid",
                    "Ambiente fictício para suporte de serviços digitais de saúde.",
                    "Fluxos simulados de atendimento, atualização e acompanhamento."
            );
            Contrato vector = obterOuCriarContrato(contratoRepository,
                    "Atlas Labs Finance",
                    "Finanças",
                    "https://atlas-labs-finance.example.invalid",
                    "Operação fictícia de controle financeiro e processos internos.",
                    "Procedimentos simulados para auditoria e acompanhamento operacional."
            );

            Servico analytics = obterOuCriarServico(servicoRepository,
                    "Analytics Operacional",
                    "Serviço fictício para leitura de indicadores operacionais."
            );
            Servico integra = obterOuCriarServico(servicoRepository,
                    "Integration Gateway",
                    "Serviço fictício para integração entre sistemas internos e externos."
            );
            Servico workflow = obterOuCriarServico(servicoRepository,
                    "Workflow Automation",
                    "Serviço fictício para automação de rotinas operacionais."
            );
            Servico portal = obterOuCriarServico(servicoRepository,
                    "Service Portal",
                    "Serviço fictício para centralização de solicitações e acompanhamento."
            );

            obterOuCriarRelacionamento(contratoServicoRepository, aurora, analytics, "2026.2", "Operações Corporativas", "Utilizado para acompanhamento de indicadores simulados.");
            obterOuCriarRelacionamento(contratoServicoRepository, aurora, workflow, "2026.1", "Implantação", "Automatiza fluxos internos fictícios de atendimento.");
            obterOuCriarRelacionamento(contratoServicoRepository, lumen, integra, "2026.2", "Integrações", "Integra sistemas demonstrativos de atendimento.");
            obterOuCriarRelacionamento(contratoServicoRepository, lumen, portal, "2026.1", "Suporte", "Portal fictício para abertura e acompanhamento de demandas.");
            obterOuCriarRelacionamento(contratoServicoRepository, vector, analytics, "2026.3", "Operações", "Apoia leitura de métricas financeiras simuladas.");
            obterOuCriarRelacionamento(contratoServicoRepository, vector, integra, "2026.2", "Integrações", "Mantém comunicação fictícia entre módulos financeiros.");
        };
    }

    private Contrato obterOuCriarContrato(
            ContratoRepository contratoRepository,
            String nome,
            String grupo,
            String centralUrl,
            String particularidades,
            String documentacao
    ) {
        Contrato contrato = contratoRepository.findByNomeIgnoreCase(nome).orElseGet(Contrato::new);
        contrato.setNome(nome);
        contrato.setGrupo(grupo);
        contrato.setCentralUrl(centralUrl);
        contrato.setParticularidades(particularidades);
        contrato.setDocumentacao(documentacao);
        contrato.setAtivo(true);
        return contratoRepository.save(contrato);
    }

    private Servico obterOuCriarServico(ServicoRepository servicoRepository, String nome, String descricaoSoftware) {
        Servico servico = servicoRepository.findByNomeIgnoreCase(nome).orElseGet(Servico::new);
        servico.setNome(nome);
        servico.setDescricaoSoftware(descricaoSoftware);
        servico.setAtivo(true);
        return servicoRepository.save(servico);
    }

    private ContratoServico obterOuCriarRelacionamento(
            ContratoServicoRepository contratoServicoRepository,
            Contrato contrato,
            Servico servico,
            String versao,
            String setor,
            String observacao
    ) {
        ContratoServico relacionamento = contratoServicoRepository
                .findByContratoIdAndServicoId(contrato.getId(), servico.getId())
                .orElseGet(ContratoServico::new);
        relacionamento.setContrato(contrato);
        relacionamento.setServico(servico);
        relacionamento.setVersao(versao);
        relacionamento.setSetor(setor);
        relacionamento.setObservacao(observacao);
        relacionamento.setAtivo(true);
        return contratoServicoRepository.save(relacionamento);
    }
}
