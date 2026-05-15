package com.atlas.platform.controller;

import com.atlas.platform.model.Contrato;
import com.atlas.platform.model.ContratoServico;
import com.atlas.platform.model.Servico;
import com.atlas.platform.repository.ContratoRepository;
import com.atlas.platform.repository.ContratoServicoRepository;
import com.atlas.platform.repository.ServicoRepository;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class ApiEndpointsIntegrationTest {

    private static final String ADMIN_USERNAME = "demo.admin";
    private static final String ADMIN_PASSWORD = "demo-admin-password";
    private static final String VISUALIZADOR_USERNAME = "demo.viewer";
    private static final String VISUALIZADOR_PASSWORD = "demo-viewer-password";
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

    private String adminAuthorization;
    private String visualizadorAuthorization;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();
        contratoServicoRepository.deleteAll();
        contratoRepository.deleteAll();
        servicoRepository.deleteAll();
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
                .andExpect(jsonPath("$.dados[0].titulo").value("Onboarding de nova organizacao"))
                .andExpect(jsonPath("$.dados[0].prioridade").value("Alta"))
                .andExpect(jsonPath("$.dados[1].prioridade").value("Media"))
                .andExpect(jsonPath("$.dados[2].prioridade").value("Baixa"));
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
                .andExpect(jsonPath("$.dados[0].ativo").value(true));
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
        return contratoServicoRepository.save(relacionamento);
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
        return """
                {
                  "nome": "%s",
                  "descricaoSoftware": "%s",
                  "ativo": %s
                }
                """.formatted(nome, descricaoSoftware, ativo);
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
}
