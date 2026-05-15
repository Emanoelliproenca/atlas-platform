package com.atlas.platform.service;

import com.atlas.platform.dto.ImportacaoPlanilhaResponse;
import com.atlas.platform.exception.BusinessException;
import com.atlas.platform.model.Servico;
import com.atlas.platform.repository.ServicoRepository;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

@Component
public class ImportacaoServicoProcessor {

    private final ServicoRepository servicoRepository;
    private final ImportacaoPlanilhaWorkbookReader workbookReader;

    public ImportacaoServicoProcessor(
            ServicoRepository servicoRepository,
            ImportacaoPlanilhaWorkbookReader workbookReader
    ) {
        this.servicoRepository = servicoRepository;
        this.workbookReader = workbookReader;
    }

    public Map<Long, Servico> processar(Sheet sheet, ImportacaoPlanilhaResponse response) {
        workbookReader.validarCabecalho(sheet, "ServicoID", "Servico", "Software \\Descrição");

        Map<Long, Servico> servicos = new HashMap<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (workbookReader.linhaVazia(row)) {
                continue;
            }

            Long idPlanilha = workbookReader.getLong(row, 0, "ServicoID", i);
            if (servicos.containsKey(idPlanilha)) {
                throw new BusinessException("ServicoID duplicado na aba Serviços na linha " + (i + 1));
            }

            String nome = workbookReader.getString(row, 1);
            if (nome.isBlank()) {
                throw new BusinessException("Serviço sem nome na linha " + (i + 1));
            }

            Servico servico = servicoRepository.findByNomeIgnoreCase(nome)
                    .orElseGet(Servico::new);

            boolean novo = servico.getId() == null;
            atualizarServico(servico, nome, row);

            Servico salvo = servicoRepository.save(servico);
            servicos.put(idPlanilha, salvo);
            contabilizar(response, novo);
        }

        return servicos;
    }

    private void atualizarServico(Servico servico, String nome, Row row) {
        servico.setNome(nome);
        servico.setDescricaoSoftware(workbookReader.getString(row, 2));
        if (servico.getAtivo() == null) {
            servico.setAtivo(true);
        }
    }

    private void contabilizar(ImportacaoPlanilhaResponse response, boolean novo) {
        if (novo) {
            response.incrementarServicosCriados();
            return;
        }

        response.incrementarServicosAtualizados();
    }
}
