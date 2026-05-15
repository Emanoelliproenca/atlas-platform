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
@Profile("demo")
public class PortfolioDemoDataInitializer {

    @Bean
    public CommandLineRunner inicializarDadosPortfolio(
            ContratoRepository contratoRepository,
            ServicoRepository servicoRepository,
            ContratoServicoRepository contratoServicoRepository
    ) {
        return args -> {
            if (contratoRepository.count() > 0 || servicoRepository.count() > 0) {
                return;
            }

            Contrato aurora = contratoRepository.save(criarContrato(
                    "Aurora Retail Cloud",
                    "Varejo",
                    "https://aurora-demo.example.invalid",
                    "Organizacao ficticia em fase de expansao, com prioridade para dashboards executivos e trilhas de atendimento.",
                    "Documentacao centralizada no playbook de implantacao SaaS."
            ));
            Contrato vector = contratoRepository.save(criarContrato(
                    "Vector Finance Ops",
                    "Financas",
                    "https://vector-demo.example.invalid",
                    "Operacao regulada com janelas restritas para publicacao de novas versoes.",
                    "Runbook de suporte nivel 2 e matriz de escalonamento disponiveis no ATLAS."
            ));
            Contrato lumen = contratoRepository.save(criarContrato(
                    "Lumen Health Services",
                    "Saude",
                    "https://lumen-demo.example.invalid",
                    "Ambiente com integracoes criticas e acompanhamento semanal pelo time de operacoes corporativas.",
                    "Checklists de implantacao, homologacao e rollback mantidos por contrato."
            ));

            Servico analytics = servicoRepository.save(criarServico(
                    "Analytics Operacional",
                    "Dashboards de indicadores, consolidacao de eventos e metricas de uso para organizacoes B2B."
            ));
            Servico workflow = servicoRepository.save(criarServico(
                    "Workflow Automation",
                    "Automacao de rotinas operacionais, aprovacoes e repasses entre times de operacao."
            ));
            Servico portal = servicoRepository.save(criarServico(
                    "Service Portal",
                    "Portal de atendimento, documentacao e acompanhamento de solicitacoes corporativas."
            ));
            Servico integra = servicoRepository.save(criarServico(
                    "Integration Gateway",
                    "Camada de integracao com ERPs, CRMs e plataformas externas."
            ));

            contratoServicoRepository.save(criarRelacionamento(aurora, analytics, "2026.2", "Operacoes Corporativas", "Painel executivo liberado para liderancas regionais."));
            contratoServicoRepository.save(criarRelacionamento(aurora, workflow, "2026.1", "Implantacao", "Automacoes de onboarding em validacao assistida."));
            contratoServicoRepository.save(criarRelacionamento(vector, analytics, "2026.3", "Operacoes", "Indicadores financeiros revisados mensalmente."));
            contratoServicoRepository.save(criarRelacionamento(vector, integra, "2026.2", "Integracoes", "Integracao com ERP em janela controlada."));
            contratoServicoRepository.save(criarRelacionamento(lumen, portal, "2026.1", "Suporte", "Base de conhecimento e solicitacoes ativas."));
            contratoServicoRepository.save(criarRelacionamento(lumen, integra, "2026.2", "Integracoes", "Sincronizacao de dados operacionais com auditoria."));
        };
    }

    private Contrato criarContrato(String nome, String grupo, String centralUrl, String particularidades, String documentacao) {
        Contrato contrato = new Contrato();
        contrato.setNome(nome);
        contrato.setGrupo(grupo);
        contrato.setCentralUrl(centralUrl);
        contrato.setParticularidades(particularidades);
        contrato.setDocumentacao(documentacao);
        contrato.setAtivo(true);
        return contrato;
    }

    private Servico criarServico(String nome, String descricaoSoftware) {
        Servico servico = new Servico();
        servico.setNome(nome);
        servico.setDescricaoSoftware(descricaoSoftware);
        servico.setAtivo(true);
        return servico;
    }

    private ContratoServico criarRelacionamento(
            Contrato contrato,
            Servico servico,
            String versao,
            String setor,
            String observacao
    ) {
        ContratoServico relacionamento = new ContratoServico();
        relacionamento.setContrato(contrato);
        relacionamento.setServico(servico);
        relacionamento.setVersao(versao);
        relacionamento.setSetor(setor);
        relacionamento.setObservacao(observacao);
        relacionamento.setAtivo(true);
        return relacionamento;
    }
}
