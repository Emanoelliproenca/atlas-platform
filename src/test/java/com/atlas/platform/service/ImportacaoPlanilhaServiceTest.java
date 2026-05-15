package com.atlas.platform.service;

import com.atlas.platform.dto.ImportacaoPlanilhaResponse;
import com.atlas.platform.exception.BusinessException;
import com.atlas.platform.model.Contrato;
import com.atlas.platform.model.ContratoServico;
import com.atlas.platform.model.Servico;
import com.atlas.platform.repository.ContratoRepository;
import com.atlas.platform.repository.ContratoServicoRepository;
import com.atlas.platform.repository.ServicoRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ImportacaoPlanilhaServiceTest {

    @Autowired
    private ImportacaoPlanilhaService service;

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private ContratoServicoRepository contratoServicoRepository;

    @BeforeEach
    void setUp() {
        contratoServicoRepository.deleteAll();
        contratoRepository.deleteAll();
        servicoRepository.deleteAll();
    }

    @Test
    void deveImportarCriandoContratosServicosERelacionamentos() throws IOException {
        MockMultipartFile arquivo = criarPlanilha(
                List.of(
                        new String[]{"contrato_id", "contrato_nome", "central", "Grupo"},
                        new String[]{"1", "Contrato Atlas", "https://atlas-demo.example.invalid", "Operacoes"}
                ),
                List.of(
                        new String[]{"ServicoID", "Servico", "Software \\Descrição"},
                        new String[]{"10", "Analytics Operacional", "Suite ATLAS"}
                ),
                List.of(
                        new String[]{"ContratoID", "ServicoID", "Versao", "Obs", "Setor"},
                        new String[]{"1", "10", "2026.1", "Ambiente principal", "Operacoes Corporativas"}
                )
        );

        ImportacaoPlanilhaResponse response = service.importar(arquivo);

        assertEquals(1, response.getContratosCriados());
        assertEquals(1, response.getServicosCriados());
        assertEquals(1, response.getRelacionamentosCriados());
        assertEquals(0, response.getRelacionamentosIgnorados());

        Contrato contrato = contratoRepository.findByNomeIgnoreCase("Contrato Atlas").orElseThrow();
        Servico servico = servicoRepository.findByNomeIgnoreCase("Analytics Operacional").orElseThrow();
        ContratoServico relacionamento = contratoServicoRepository
                .findByContratoIdAndServicoId(contrato.getId(), servico.getId())
                .orElseThrow();

        assertEquals("https://atlas-demo.example.invalid", contrato.getCentralUrl());
        assertEquals("Operacoes", contrato.getGrupo());
        assertTrue(contrato.getAtivo());
        assertEquals("Suite ATLAS", servico.getDescricaoSoftware());
        assertTrue(servico.getAtivo());
        assertEquals("2026.1", relacionamento.getVersao());
        assertEquals("Ambiente principal", relacionamento.getObservacao());
        assertEquals("Operacoes Corporativas", relacionamento.getSetor());
    }

    @Test
    void deveAtualizarRegistrosExistentesEIgnorarRelacionamentoInvalido() throws IOException {
        Contrato contrato = new Contrato();
        contrato.setNome("Contrato Atlas");
        contrato.setCentralUrl("https://legado-demo.example.invalid");
        contrato.setGrupo("Legado");
        contrato.setParticularidades("Mantem");
        contrato.setDocumentacao("Doc");
        contrato.setAtivo(true);
        contrato = contratoRepository.save(contrato);

        Servico servico = new Servico();
        servico.setNome("Analytics Operacional");
        servico.setDescricaoSoftware("Suite Legada");
        servico.setAtivo(true);
        servico = servicoRepository.save(servico);

        ContratoServico relacionamento = new ContratoServico();
        relacionamento.setContrato(contrato);
        relacionamento.setServico(servico);
        relacionamento.setVersao("2025.4");
        relacionamento.setObservacao("Observacao antiga");
        relacionamento.setSetor("Suporte");
        contratoServicoRepository.save(relacionamento);

        MockMultipartFile arquivo = criarPlanilha(
                List.of(
                        new String[]{"contrato_id", "contrato_nome", "central", "Grupo"},
                        new String[]{"1", "Contrato Atlas", "https://nova-demo.example.invalid", "Operacoes"}
                ),
                List.of(
                        new String[]{"ServicoID", "Servico", "Software \\Descrição"},
                        new String[]{"10", "Analytics Operacional", "Suite ATLAS"}
                ),
                List.of(
                        new String[]{"ContratoID", "ServicoID", "Versao", "Obs", "Setor"},
                        new String[]{"1", "10", "2026.2", "Atualizado pela carga", "Operacoes Corporativas"},
                        new String[]{"999", "10", "2026.2", "Invalido", "Operacoes Corporativas"}
                )
        );

        ImportacaoPlanilhaResponse response = service.importar(arquivo);

        assertEquals(1, response.getContratosAtualizados());
        assertEquals(1, response.getServicosAtualizados());
        assertEquals(1, response.getRelacionamentosAtualizados());
        assertEquals(1, response.getRelacionamentosIgnorados());
        assertEquals(1, response.getAvisos().size());
        assertTrue(response.getAvisos().get(0).contains("ContratoID 999"));

        Contrato contratoAtualizado = contratoRepository.findByNomeIgnoreCase("Contrato Atlas").orElseThrow();
        Servico servicoAtualizado = servicoRepository.findByNomeIgnoreCase("Analytics Operacional").orElseThrow();
        ContratoServico relacionamentoAtualizado = contratoServicoRepository
                .findByContratoIdAndServicoId(contratoAtualizado.getId(), servicoAtualizado.getId())
                .orElseThrow();

        assertEquals("https://nova-demo.example.invalid", contratoAtualizado.getCentralUrl());
        assertEquals("Operacoes", contratoAtualizado.getGrupo());
        assertEquals("Mantem", contratoAtualizado.getParticularidades());
        assertEquals("Doc", contratoAtualizado.getDocumentacao());
        assertEquals("Suite ATLAS", servicoAtualizado.getDescricaoSoftware());
        assertEquals("2026.2", relacionamentoAtualizado.getVersao());
        assertEquals("Atualizado pela carga", relacionamentoAtualizado.getObservacao());
        assertEquals("Operacoes Corporativas", relacionamentoAtualizado.getSetor());
    }

    @Test
    void deveRejeitarPlanilhaComIdsDuplicadosNaAbaDeContratos() throws IOException {
        MockMultipartFile arquivo = criarPlanilha(
                List.of(
                        new String[]{"contrato_id", "contrato_nome", "central", "Grupo"},
                        new String[]{"1", "Contrato Atlas", "https://atlas-demo.example.invalid", "Operacoes"},
                        new String[]{"1", "Contrato Sigma", "https://sigma-demo.example.invalid", "Financeiro"}
                ),
                List.of(
                        new String[]{"ServicoID", "Servico", "Software \\Descrição"},
                        new String[]{"10", "Analytics Operacional", "Suite ATLAS"}
                ),
                List.<String[]>of(
                        new String[]{"ContratoID", "ServicoID", "Versao", "Obs", "Setor"}
                )
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> service.importar(arquivo));
        assertEquals("ContratoID duplicado na aba Contratos na linha 3", exception.getMessage());
    }

    private MockMultipartFile criarPlanilha(
            List<String[]> contratos,
            List<String[]> servicos,
            List<String[]> relacionamentos
    ) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            preencherAba(workbook, "Contratos", contratos);
            preencherAba(workbook, "Serviços", servicos);
            preencherAba(workbook, "Relacionamento", relacionamentos);
            workbook.write(outputStream);

            return new MockMultipartFile(
                    "arquivo",
                    "importacao.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    outputStream.toByteArray()
            );
        }
    }

    private void preencherAba(XSSFWorkbook workbook, String nome, List<String[]> linhas) {
        var sheet = workbook.createSheet(nome);

        for (int i = 0; i < linhas.size(); i++) {
            Row row = sheet.createRow(i);
            String[] colunas = linhas.get(i);
            for (int j = 0; j < colunas.length; j++) {
                row.createCell(j).setCellValue(colunas[j]);
            }
        }
    }
}
