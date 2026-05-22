package com.atlas.platform.controller;

import com.atlas.platform.model.Contrato;
import com.atlas.platform.model.ContratoServico;
import com.atlas.platform.model.Repasse;
import com.atlas.platform.model.Servico;
import com.atlas.platform.repository.ContratoRepository;
import com.atlas.platform.repository.ContratoServicoRepository;
import com.atlas.platform.repository.RepasseRepository;
import com.atlas.platform.repository.ServicoRepository;
import com.atlas.platform.repository.SoftwareArtefatoRepository;
import com.atlas.platform.repository.AuditoriaEventoRepository;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class ApiEndpointsIntegrationTest {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String VISUALIZADOR_USERNAME = "viewer";
    private static final String VISUALIZADOR_PASSWORD = "viewer123";
    private static final String MENSAGEM_SESSAO_EXPIRADA =
            "Sessão expirada ou credenciais inválidas. Entre novamente para continuar.";
    private static final String MENSAGEM_SEM_PERMISSAO =
            "Seu perfil não tem permissão para executar esta ação.";
    private static final String MENSAGEM_CONTRATO_DUPLICADO = "Já existe um contrato com esse nome";
    private static final String MENSAGEM_SERVICO_DUPLICADO = "Já existe um serviço com esse nome";
    private static final String MENSAGEM_RELACIONAMENTO_DUPLICADO = "Esse serviço já está vinculado a esse contrato";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private Filter springSecurityFilterChain;

    private MockMvc mockMvc;

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private ContratoServicoRepository contratoServicoRepository;

    @Autowired
    private RepasseRepository repasseRepository;

    @Autowired
    private SoftwareArtefatoRepository softwareArtefatoRepository;

    @Autowired
    private AuditoriaEventoRepository auditoriaEventoRepository;

    private String adminAuthorization;
    private String visualizadorAuthorization;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();
        softwareArtefatoRepository.deleteAll();
        auditoriaEventoRepository.deleteAll();
        contratoServicoRepository.deleteAll();
        contratoRepository.deleteAll();
        servicoRepository.deleteAll();
        repasseRepository.deleteAll();
        criarRepassesPadrao();
        adminAuthorization = authHeader(ADMIN_USERNAME, ADMIN_PASSWORD);
        visualizadorAuthorization = authHeader(VISUALIZADOR_USERNAME, VISUALIZADOR_PASSWORD);
    }

    @Test
    void deveExigirAutenticacaoParaConsultarSessao() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_SESSAO_EXPIRADA))
                .andExpect(jsonPath("$.dados").value(nullValue()));
    }

    @Test
    void deveRetornarSessaoDoUsuarioAutenticado() throws Exception {
        mockMvc.perform(get("/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, visualizadorAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.username").value(VISUALIZADOR_USERNAME))
                .andExpect(jsonPath("$.dados.roles[0]").value("ROLE_VISUALIZADOR"));
    }

    @Test
    void deveBloquearCriacaoDeContratoParaPerfilVisualizador() throws Exception {
        mockMvc.perform(post("/contratos")
                        .header(HttpHeaders.AUTHORIZATION, visualizadorAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contratoRequestJson(
                                "Contrato Restrito",
                                "Operacoes",
                                "https://novo-demo.example.invalid",
                                "",
                                "",
                                true
                        )))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_SEM_PERMISSAO));
    }

    @Test
    void deveListarRepassesAtivosParaUsuarioVisualizador() throws Exception {
        mockMvc.perform(get("/repasses")
                        .header(HttpHeaders.AUTHORIZATION, visualizadorAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.length()").value(3))
                .andExpect(jsonPath("$.dados[0].titulo").value("Onboarding de nova operação"))
                .andExpect(jsonPath("$.dados[0].prioridade").value("Alta"))
                .andExpect(jsonPath("$.dados[1].prioridade").value("Média"))
                .andExpect(jsonPath("$.dados[2].prioridade").value("Baixa"));
    }

    @Test
    void deveCriarAtualizarFixarEInativarRepasseComoAdmin() throws Exception {
        mockMvc.perform(post("/repasses")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(repasseRequestJson(
                                "Comunicado de release",
                                "Atualização",
                                "Validar versão publicada na base operacional.",
                                "Alta",
                                true,
                                false,
                                "release.pdf"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.titulo").value("Comunicado de release"))
                .andExpect(jsonPath("$.dados.autor").value(ADMIN_USERNAME))
                .andExpect(jsonPath("$.dados.anexoNome").value("release.pdf"));

        Long repasseId = repasseRepository.findByTituloIgnoreCase("Comunicado de release").orElseThrow().getId();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/repasses/{id}", repasseId)
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(repasseRequestJson(
                                "Comunicado de release atualizado",
                                "Urgente",
                                "Validar versão publicada e comunicar operação.",
                                "Alta",
                                true,
                                true,
                                ""
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.titulo").value("Comunicado de release atualizado"))
                .andExpect(jsonPath("$.dados.fixado").value(true))
                .andExpect(jsonPath("$.dados.anexoNome").value(nullValue()));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/repasses/{id}/fixar", repasseId)
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.fixado").value(false));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/repasses/{id}/inativar", repasseId)
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.ativo").value(false));

        mockMvc.perform(get("/repasses")
                        .header(HttpHeaders.AUTHORIZATION, visualizadorAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.length()").value(3));
    }

    @Test
    void deveBloquearCrudDeRepasseParaPerfilVisualizador() throws Exception {
        mockMvc.perform(post("/repasses")
                        .header(HttpHeaders.AUTHORIZATION, visualizadorAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(repasseRequestJson("Bloqueado", "Informativo", "Sem permissao", "Baixa", true, false, "")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_SEM_PERMISSAO));
    }

    @Test
    void devePaginarListagensSemQuebrarContratoAtual() throws Exception {
        criarContrato("Contrato A", "Operacoes");
        criarContrato("Contrato B", "Financeiro");

        mockMvc.perform(get("/contratos")
                        .header(HttpHeaders.AUTHORIZATION, visualizadorAuthorization)
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.content.length()").value(1))
                .andExpect(jsonPath("$.dados.page").value(0))
                .andExpect(jsonPath("$.dados.size").value(1))
                .andExpect(jsonPath("$.dados.totalElements").value(2));

        mockMvc.perform(get("/contratos")
                        .header(HttpHeaders.AUTHORIZATION, visualizadorAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.length()").value(2));
    }

    @Test
    void deveExporMetricasDoDashboard() throws Exception {
        Contrato contrato = criarContrato("Contrato Metrics", "Operacoes");
        Servico servico = criarServico("Servico Metrics", "Suite Metrics");
        criarRelacionamento(contrato, servico, "2026.1", "Operacoes");

        mockMvc.perform(get("/dashboard/metrics")
                        .header(HttpHeaders.AUTHORIZATION, visualizadorAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.totalContratos").value(1))
                .andExpect(jsonPath("$.dados.contratosAtivos").value(1))
                .andExpect(jsonPath("$.dados.contratosInativos").value(0))
                .andExpect(jsonPath("$.dados.totalServicos").value(1))
                .andExpect(jsonPath("$.dados.servicosAtivos").value(1))
                .andExpect(jsonPath("$.dados.totalRelacionamentos").value(1))
                .andExpect(jsonPath("$.dados.relacionamentosAtivos").value(1))
                .andExpect(jsonPath("$.dados.repassesAtivos").value(3))
                .andExpect(jsonPath("$.dados.contratosPorGrupo[0].nome").value("Operacoes"))
                .andExpect(jsonPath("$.dados.servicosPorSetor[0].nome").value("Operacoes"))
                .andExpect(jsonPath("$.dados.percentualContratosComServicos").value(100.0));
    }

    @Test
    void deveRetornarErroEstruturadoSemQuebrarMensagemAtual() throws Exception {
        mockMvc.perform(get("/contratos/{id}", 999999)
                        .header(HttpHeaders.AUTHORIZATION, visualizadorAuthorization))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Contrato não encontrado com id: 999999"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Contrato não encontrado com id: 999999"))
                .andExpect(jsonPath("$.path").value("/contratos/999999"));
    }

    @Test
    void deveListarResumoDeSoftwaresComTotalDeContratosEVersao() throws Exception {
        Contrato contrato = new Contrato();
        contrato.setNome("Contrato Atlas");
        contrato.setGrupo("Operacoes");
        contrato.setCentralUrl("https://atlas-demo.example.invalid");
        contrato.setParticularidades("");
        contrato.setDocumentacao("");
        contrato.setAtivo(true);
        contrato = contratoRepository.save(contrato);

        Servico servico = new Servico();
        servico.setNome("Analytics Operacional");
        servico.setDescricaoSoftware("Suite ATLAS");
        servico.setAtivo(true);
        servico = servicoRepository.save(servico);

        ContratoServico relacionamento = new ContratoServico();
        relacionamento.setContrato(contrato);
        relacionamento.setServico(servico);
        relacionamento.setVersao("2026.3");
        relacionamento.setObservacao("Principal");
        relacionamento.setSetor("Operacoes Corporativas");
        contratoServicoRepository.save(relacionamento);

        mockMvc.perform(get("/softwares")
                        .header(HttpHeaders.AUTHORIZATION, visualizadorAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.length()").value(1))
                .andExpect(jsonPath("$.dados[0].nome").value("Analytics Operacional"))
                .andExpect(jsonPath("$.dados[0].descricaoSoftware").value("Suite ATLAS"))
                .andExpect(jsonPath("$.dados[0].versaoReferencia").value("2026.3"))
                .andExpect(jsonPath("$.dados[0].totalContratos").value(1))
                .andExpect(jsonPath("$.dados[0].ativo").value(true))
                .andExpect(jsonPath("$.dados[0].downloadDisponivel").value(false));
    }

    @Test
    void deveSalvarListarEBaixarArtefatoDeSoftwareComoAdmin() throws Exception {
        Servico servico = criarServico("Instalador ATLAS", "Agente operacional");
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "instalador-atlas.bin",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "conteudo-binario".getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/softwares/{id}/artefatos", servico.getId())
                        .file(arquivo)
                        .param("versao", "2026.5")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.nome").value("instalador-atlas.bin"))
                .andExpect(jsonPath("$.dados.versao").value("2026.5"));

        mockMvc.perform(get("/softwares")
                        .header(HttpHeaders.AUTHORIZATION, visualizadorAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados[0].downloadDisponivel").value(true))
                .andExpect(jsonPath("$.dados[0].artefatoNome").value("instalador-atlas.bin"));

        mockMvc.perform(get("/softwares/{id}/download", servico.getId())
                        .header(HttpHeaders.AUTHORIZATION, visualizadorAuthorization))
                .andExpect(status().isOk())
                .andExpect(content().bytes("conteudo-binario".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    @Test
    void deveCriarAtualizarEInativarSoftwareComoAdmin() throws Exception {
        mockMvc.perform(post("/softwares")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(servicoRequestJson(
                                "Portal Atlas Labs",
                                "Portal operacional",
                                "2026.7",
                                "https://portal-atlas-labs.example.invalid",
                                true
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.nome").value("Portal Atlas Labs"))
                .andExpect(jsonPath("$.dados.versaoReferencia").value("2026.7"))
                .andExpect(jsonPath("$.dados.linkSoftware").value("https://portal-atlas-labs.example.invalid"));

        Long servicoId = servicoRepository.findByNomeIgnoreCase("Portal Atlas Labs").orElseThrow().getId();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/softwares/{id}", servicoId)
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(servicoRequestJson(
                                "Portal Atlas Labs",
                                "Portal operacional atualizado",
                                "2026.8",
                                "https://portal-atlas-labs.example.invalid/app",
                                true
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.descricaoSoftware").value("Portal operacional atualizado"))
                .andExpect(jsonPath("$.dados.versaoReferencia").value("2026.8"))
                .andExpect(jsonPath("$.dados.linkSoftware").value("https://portal-atlas-labs.example.invalid/app"));

        mockMvc.perform(get("/softwares")
                        .header(HttpHeaders.AUTHORIZATION, visualizadorAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados[0].versaoReferencia").value("2026.8"))
                .andExpect(jsonPath("$.dados[0].linkSoftware").value("https://portal-atlas-labs.example.invalid/app"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/softwares/{id}/inativar", servicoId)
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.ativo").value(false));
    }

    @Test
    void deveRetornarErroClaroQuandoSincronizacaoNaoEstaConfigurada() throws Exception {
        mockMvc.perform(post("/softwares/sincronizar")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Configure app.software-sync.manifest-url para sincronizar versões com um sistema externo real."));
    }

    @Test
    void deveRegistrarAuditoriaEmAlteracoesCriticasEApenasAdminConsulta() throws Exception {
        mockMvc.perform(post("/repasses")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(repasseRequestJson("Auditoria", "Operação", "Registrar evento.", "Baixa", true, false, "")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/auditoria")
                        .header(HttpHeaders.AUTHORIZATION, visualizadorAuthorization))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_SEM_PERMISSAO));

        mockMvc.perform(get("/auditoria")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados[0].entidade").value("SEGURANCA"))
                .andExpect(jsonPath("$.dados[0].acao").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.dados[1].entidade").value("REPASSE"))
                .andExpect(jsonPath("$.dados[1].acao").value("CREATE_REPASSE"))
                .andExpect(jsonPath("$.dados[1].usuario").value(ADMIN_USERNAME));
    }

    @Test
    void deveCriarListarEExcluirRelacionamentoComoAdmin() throws Exception {
        Contrato contrato = criarContrato("Contrato Apollo", "Operacoes");
        Servico servico = criarServico("Service Desk", "Plataforma Apollo");

        mockMvc.perform(post("/contrato-servicos")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(relacionamentoRequestJson(
                                contrato.getId(),
                                servico.getId(),
                                "2026.4",
                                "Cobertura principal",
                                "Service Desk"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.contratoNome").value("Contrato Apollo"))
                .andExpect(jsonPath("$.dados.servicoNome").value("Service Desk"))
                .andExpect(jsonPath("$.dados.versao").value("2026.4"))
                .andExpect(jsonPath("$.dados.setor").value("Service Desk"));

        mockMvc.perform(get("/contrato-servicos")
                        .header(HttpHeaders.AUTHORIZATION, visualizadorAuthorization)
                        .param("contratoIds", contrato.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.length()").value(1))
                .andExpect(jsonPath("$.dados[0].contratoNome").value("Contrato Apollo"));

        Long relacionamentoId = contratoServicoRepository.findAll().get(0).getId();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/contrato-servicos/{id}", relacionamentoId)
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.mensagem").value("Relacionamento deletado com sucesso"));

        org.junit.jupiter.api.Assertions.assertTrue(contratoServicoRepository.findAll().isEmpty());
    }

    @Test
    void deveBloquearRelacionamentoDuplicadoComoRegraDeNegocio() throws Exception {
        Contrato contrato = criarContrato("Contrato Hermes", "Operacoes");
        Servico servico = criarServico("Analytics Operacional", "Suite Orion");
        criarRelacionamento(contrato, servico, "2026.1", "Operacoes Corporativas");

        mockMvc.perform(post("/contrato-servicos")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(relacionamentoRequestJson(
                                contrato.getId(),
                                servico.getId(),
                                "2026.2",
                                "Duplicado",
                                "Operacoes Corporativas"
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_RELACIONAMENTO_DUPLICADO));
    }

    @Test
    void deveBloquearInativacaoDeContratoComVinculoAtivo() throws Exception {
        Contrato contrato = criarContrato("Contrato Vinculado", "Operacoes");
        Servico servico = criarServico("Servico Vinculado", "Suite");
        criarRelacionamento(contrato, servico, "2026.1", "Operacoes");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/contratos/{id}/inativar", contrato.getId())
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Não é possível inativar o contrato porque existem vínculos ativos com serviços."));
    }

    @Test
    void deveBloquearInativacaoDeServicoComVinculoAtivo() throws Exception {
        Contrato contrato = criarContrato("Contrato Servico Vinculado", "Operacoes");
        Servico servico = criarServico("Servico Com Vinculo", "Suite");
        criarRelacionamento(contrato, servico, "2026.1", "Operacoes");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/servicos/{id}/inativar", servico.getId())
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Não é possível inativar o serviço porque existem vínculos ativos com contratos."));
    }

    @Test
    void deveFiltrarPainelOperacionalPorGrupoESetor() throws Exception {
        Contrato contratoOperacoes = criarContrato("Contrato Atlas", "Operacoes");
        Contrato contratoFinanceiro = criarContrato("Contrato Sigma", "Financeiro");
        Servico servicoAnalyticsOperacional = criarServico("Analytics Operacional", "Suite ATLAS");
        Servico servicoBI = criarServico("BI", "Suite Sigma");

        criarRelacionamento(contratoOperacoes, servicoAnalyticsOperacional, "2026.3", "Operacoes Corporativas");
        criarRelacionamento(contratoFinanceiro, servicoBI, "2026.1", "Backoffice");

        mockMvc.perform(get("/painel/operacional")
                        .header(HttpHeaders.AUTHORIZATION, visualizadorAuthorization)
                        .param("grupo", "Operacoes")
                        .param("setor", "Operacoes Corporativas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.resumo.totalRelacionamentos").value(1))
                .andExpect(jsonPath("$.dados.resumo.totalContratos").value(1))
                .andExpect(jsonPath("$.dados.linhas.length()").value(1))
                .andExpect(jsonPath("$.dados.linhas[0].contratoNome").value("Contrato Atlas"))
                .andExpect(jsonPath("$.dados.linhas[0].grupo").value("Operacoes"))
                .andExpect(jsonPath("$.dados.linhas[0].setor").value("Operacoes Corporativas"));
    }

    @Test
    void deveIgnorarEspacosLateraisNosFiltrosDoPainel() throws Exception {
        Contrato contrato = criarContrato("Contrato Atlas", "Operacoes");
        Servico servico = criarServico("Analytics Operacional", "Suite ATLAS");
        criarRelacionamento(contrato, servico, "2026.3", "Operacoes Corporativas");

        mockMvc.perform(get("/painel/operacional")
                        .header(HttpHeaders.AUTHORIZATION, visualizadorAuthorization)
                        .param("grupo", "  Operacoes  ")
                        .param("setor", "  Operacoes Corporativas  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.linhas.length()").value(1))
                .andExpect(jsonPath("$.dados.linhas[0].contratoNome").value("Contrato Atlas"));
    }

    @Test
    void deveBloquearContratoDuplicadoIgnorandoCaixaEEspacos() throws Exception {
        criarContrato("Contrato Atlas", "Operacoes");

        mockMvc.perform(post("/contratos")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contratoRequestJson(
                                "  contrato atlas  ",
                                "Operacoes",
                                "https://nova-demo.example.invalid",
                                "",
                                "",
                                true
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_CONTRATO_DUPLICADO));
    }

    @Test
    void deveBloquearServicoDuplicadoIgnorandoCaixaEEspacos() throws Exception {
        criarServico("Analytics Operacional", "Suite ATLAS");

        mockMvc.perform(post("/servicos")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(servicoRequestJson("  analytics operacional  ", "Nova suite", true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_SERVICO_DUPLICADO));
    }

    @Test
    void deveNormalizarCamposTextuaisAoCriarRelacionamento() throws Exception {
        Contrato contrato = criarContrato("Contrato Atlas", "Operacoes");
        Servico servico = criarServico("Analytics Operacional", "Suite ATLAS");

        mockMvc.perform(post("/contrato-servicos")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(relacionamentoRequestJson(
                                contrato.getId(),
                                servico.getId(),
                                " 2026.4 ",
                                " Atualizado em janela controlada ",
                                " Operacoes Corporativas "
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.versao").value("2026.4"))
                .andExpect(jsonPath("$.dados.observacao").value("Atualizado em janela controlada"))
                .andExpect(jsonPath("$.dados.setor").value("Operacoes Corporativas"));
    }

    @Test
    void deveRejeitarRelacionamentoComIdsInvalidos() throws Exception {
        mockMvc.perform(post("/contrato-servicos")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(relacionamentoRequestJson(0L, -1L, "2026.4", null, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(content().string(org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.containsString("Contrato é obrigatório"),
                        org.hamcrest.Matchers.containsString("Serviço é obrigatório")
                )));
    }

    private String authHeader(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson(username, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = response.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
        return "Bearer " + token;
    }

    private Contrato criarContrato(String nome, String grupo) {
        Contrato contrato = new Contrato();
        contrato.setNome(nome);
        contrato.setGrupo(grupo);
        contrato.setCentralUrl("https://" + nome.toLowerCase().replace(" ", "-") + "-demo.example.invalid");
        contrato.setParticularidades("");
        contrato.setDocumentacao("");
        contrato.setAtivo(true);
        return contratoRepository.save(contrato);
    }

    private Servico criarServico(String nome, String descricao) {
        Servico servico = new Servico();
        servico.setNome(nome);
        servico.setDescricaoSoftware(descricao);
        servico.setAtivo(true);
        return servicoRepository.save(servico);
    }

    private ContratoServico criarRelacionamento(Contrato contrato, Servico servico, String versao, String setor) {
        ContratoServico relacionamento = new ContratoServico();
        relacionamento.setContrato(contrato);
        relacionamento.setServico(servico);
        relacionamento.setVersao(versao);
        relacionamento.setObservacao("Observacao");
        relacionamento.setSetor(setor);
        relacionamento.setAtivo(true);
        return contratoServicoRepository.save(relacionamento);
    }

    private void criarRepassesPadrao() {
        criarRepasse("Onboarding de nova operação", "Implantação", "Registrar escopo.", "Alta");
        criarRepasse("Checklist de release demo", "Produto", "Validar versão fictícia.", "Média");
        criarRepasse("Mensagem padrão para suporte", "Comunicação", "Manter modelos de resposta.", "Baixa");
    }

    private void criarRepasse(String titulo, String categoria, String conteudo, String prioridade) {
        Repasse repasse = new Repasse();
        repasse.setTitulo(titulo);
        repasse.setCategoria(categoria);
        repasse.setConteudo(conteudo);
        repasse.setPrioridade(prioridade);
        repasse.setAtivo(true);
        repasse.setFixado("Alta".equals(prioridade));
        repasse.setAutor("ATLAS");
        repasse.setCriadoEm(java.time.Instant.now());
        repasseRepository.save(repasse);
    }

    private String loginRequestJson(String username, String password) {
        return """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);
    }

    private String contratoRequestJson(
            String nome,
            String grupo,
            String centralUrl,
            String particularidades,
            String documentacao,
            boolean ativo
    ) {
        return """
                {
                  "nome": "%s",
                  "grupo": "%s",
                  "centralUrl": "%s",
                  "particularidades": "%s",
                  "documentacao": "%s",
                  "ativo": %s
                }
                """.formatted(nome, grupo, centralUrl, particularidades, documentacao, ativo);
    }

    private String servicoRequestJson(String nome, String descricaoSoftware, boolean ativo) {
        return servicoRequestJson(nome, descricaoSoftware, "", "", ativo);
    }

    private String servicoRequestJson(
            String nome,
            String descricaoSoftware,
            String versaoReferencia,
            String linkSoftware,
            boolean ativo
    ) {
        return """
                {
                  "nome": "%s",
                  "descricaoSoftware": "%s",
                  "versaoReferencia": "%s",
                  "linkSoftware": "%s",
                  "ativo": %s
                }
                """.formatted(nome, descricaoSoftware, versaoReferencia, linkSoftware, ativo);
    }

    private String relacionamentoRequestJson(
            Long contratoId,
            Long servicoId,
            String versao,
            String observacao,
            String setor
    ) {
        return """
                {
                  "contratoId": %d,
                  "servicoId": %d,
                  "versao": %s,
                  "observacao": %s,
                  "setor": %s
                }
                """.formatted(
                contratoId,
                servicoId,
                jsonString(versao),
                jsonString(observacao),
                jsonString(setor)
        );
    }

    private String jsonString(String value) {
        return value == null ? "null" : "\"%s\"".formatted(value);
    }

    private String repasseRequestJson(
            String titulo,
            String categoria,
            String conteudo,
            String prioridade,
            boolean ativo,
            boolean fixado,
            String anexoNome
    ) {
        return """
                {
                  "titulo": "%s",
                  "categoria": "%s",
                  "conteudo": "%s",
                  "prioridade": "%s",
                  "ativo": %s,
                  "fixado": %s,
                  "anexoNome": "%s"
                }
                """.formatted(titulo, categoria, conteudo, prioridade, ativo, fixado, anexoNome);
    }
}
