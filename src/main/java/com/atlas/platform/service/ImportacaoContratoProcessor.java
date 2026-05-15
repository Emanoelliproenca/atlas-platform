package com.atlas.platform.service;

import com.atlas.platform.dto.ImportacaoPlanilhaResponse;
import com.atlas.platform.exception.BusinessException;
import com.atlas.platform.model.Contrato;
import com.atlas.platform.repository.ContratoRepository;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

@Component
public class ImportacaoContratoProcessor {

    private final ContratoRepository contratoRepository;
    private final ImportacaoPlanilhaWorkbookReader workbookReader;

    public ImportacaoContratoProcessor(
            ContratoRepository contratoRepository,
            ImportacaoPlanilhaWorkbookReader workbookReader
    ) {
        this.contratoRepository = contratoRepository;
        this.workbookReader = workbookReader;
    }

    public Map<Long, Contrato> processar(Sheet sheet, ImportacaoPlanilhaResponse response) {
        workbookReader.validarCabecalho(sheet, "contrato_id", "contrato_nome", "central", "Grupo");

        Map<Long, Contrato> contratos = new HashMap<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (workbookReader.linhaVazia(row)) {
                continue;
            }

            Long idPlanilha = workbookReader.getLong(row, 0, "contrato_id", i);
            if (contratos.containsKey(idPlanilha)) {
                throw new BusinessException("ContratoID duplicado na aba Contratos na linha " + (i + 1));
            }

            String nome = workbookReader.getString(row, 1);
            if (nome.isBlank()) {
                throw new BusinessException("Contrato sem nome na linha " + (i + 1));
            }

            Contrato contrato = contratoRepository.findByNomeIgnoreCase(nome)
                    .orElseGet(Contrato::new);

            boolean novo = contrato.getId() == null;
            atualizarContrato(contrato, nome, row);

            Contrato salvo = contratoRepository.save(contrato);
            contratos.put(idPlanilha, salvo);
            contabilizar(response, novo);
        }

        return contratos;
    }

    private void atualizarContrato(Contrato contrato, String nome, Row row) {
        contrato.setNome(nome);
        contrato.setCentralUrl(workbookReader.getString(row, 2));
        contrato.setGrupo(workbookReader.getString(row, 3));
        if (contrato.getParticularidades() == null) {
            contrato.setParticularidades("");
        }
        if (contrato.getDocumentacao() == null) {
            contrato.setDocumentacao("");
        }
        if (contrato.getAtivo() == null) {
            contrato.setAtivo(true);
        }
    }

    private void contabilizar(ImportacaoPlanilhaResponse response, boolean novo) {
        if (novo) {
            response.incrementarContratosCriados();
            return;
        }

        response.incrementarContratosAtualizados();
    }
}
