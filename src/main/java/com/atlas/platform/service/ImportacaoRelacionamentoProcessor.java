package com.atlas.platform.service;

import com.atlas.platform.dto.ImportacaoPlanilhaResponse;
import com.atlas.platform.exception.BusinessException;
import com.atlas.platform.model.Contrato;
import com.atlas.platform.model.ContratoServico;
import com.atlas.platform.model.Servico;
import com.atlas.platform.repository.ContratoServicoRepository;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

@Component
public class ImportacaoRelacionamentoProcessor {

    private final ContratoServicoRepository contratoServicoRepository;
    private final ImportacaoPlanilhaWorkbookReader workbookReader;

    public ImportacaoRelacionamentoProcessor(
            ContratoServicoRepository contratoServicoRepository,
            ImportacaoPlanilhaWorkbookReader workbookReader
    ) {
        this.contratoServicoRepository = contratoServicoRepository;
        this.workbookReader = workbookReader;
    }

    public void processar(
            Sheet sheet,
            Map<Long, Contrato> contratosPorPlanilha,
            Map<Long, Servico> servicosPorPlanilha,
            ImportacaoPlanilhaResponse response
    ) {
        workbookReader.validarCabecalho(sheet, "ContratoID", "ServicoID", "Versao", "Obs", "Setor");

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (workbookReader.linhaVazia(row)) {
                continue;
            }

            try {
                Long contratoIdPlanilha = workbookReader.getLong(row, 0, "ContratoID", i);
                Long servicoIdPlanilha = workbookReader.getLong(row, 1, "ServicoID", i);

                Contrato contrato = contratosPorPlanilha.get(contratoIdPlanilha);
                if (contrato == null) {
                    response.registrarRelacionamentoIgnorado(
                            "Linha " + (i + 1) + " ignorada: ContratoID " + contratoIdPlanilha + " não encontrado na aba Relacionamento"
                    );
                    continue;
                }

                Servico servico = servicosPorPlanilha.get(servicoIdPlanilha);
                if (servico == null) {
                    response.registrarRelacionamentoIgnorado(
                            "Linha " + (i + 1) + " ignorada: ServicoID " + servicoIdPlanilha + " não encontrado na aba Relacionamento"
                    );
                    continue;
                }

                ContratoServico relacionamento = contratoServicoRepository
                        .findByContratoIdAndServicoId(contrato.getId(), servico.getId())
                        .orElseGet(ContratoServico::new);

                boolean novo = relacionamento.getId() == null;
                atualizarRelacionamento(relacionamento, contrato, servico, row);
                contratoServicoRepository.save(relacionamento);
                contabilizar(response, novo);
            } catch (BusinessException exception) {
                response.registrarRelacionamentoIgnorado("Linha " + (i + 1) + " ignorada: " + exception.getMessage());
            }
        }
    }

    private void atualizarRelacionamento(ContratoServico relacionamento, Contrato contrato, Servico servico, Row row) {
        relacionamento.setContrato(contrato);
        relacionamento.setServico(servico);
        relacionamento.setVersao(workbookReader.getString(row, 2));
        relacionamento.setObservacao(workbookReader.getString(row, 3));
        relacionamento.setSetor(workbookReader.getString(row, 4));
    }

    private void contabilizar(ImportacaoPlanilhaResponse response, boolean novo) {
        if (novo) {
            response.incrementarRelacionamentosCriados();
            return;
        }

        response.incrementarRelacionamentosAtualizados();
    }
}
