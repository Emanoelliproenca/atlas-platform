package com.atlas.platform.service;

import com.atlas.platform.dto.ImportacaoPlanilhaResponse;
import com.atlas.platform.exception.BusinessException;
import com.atlas.platform.model.Contrato;
import com.atlas.platform.model.Servico;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Service
public class ImportacaoPlanilhaService {

    private static final String ABA_CONTRATOS = "Contratos";
    private static final String ABA_SERVICOS = "Serviços";
    private static final String ABA_RELACIONAMENTOS = "Relacionamento";

    private final ImportacaoPlanilhaWorkbookReader workbookReader;
    private final ImportacaoContratoProcessor contratoProcessor;
    private final ImportacaoServicoProcessor servicoProcessor;
    private final ImportacaoRelacionamentoProcessor relacionamentoProcessor;

    public ImportacaoPlanilhaService(
            ImportacaoPlanilhaWorkbookReader workbookReader,
            ImportacaoContratoProcessor contratoProcessor,
            ImportacaoServicoProcessor servicoProcessor,
            ImportacaoRelacionamentoProcessor relacionamentoProcessor
    ) {
        this.workbookReader = workbookReader;
        this.contratoProcessor = contratoProcessor;
        this.servicoProcessor = servicoProcessor;
        this.relacionamentoProcessor = relacionamentoProcessor;
    }

    @Transactional
    public ImportacaoPlanilhaResponse importar(MultipartFile arquivo) {
        workbookReader.validarArquivo(arquivo);

        try (InputStream inputStream = arquivo.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet abaContratos = workbookReader.obterAbaObrigatoria(workbook, ABA_CONTRATOS);
            Sheet abaServicos = workbookReader.obterAbaObrigatoria(workbook, ABA_SERVICOS);
            Sheet abaRelacionamentos = workbookReader.obterAbaObrigatoria(workbook, ABA_RELACIONAMENTOS);

            ImportacaoPlanilhaResponse response = new ImportacaoPlanilhaResponse();
            Map<Long, Contrato> contratosPorPlanilha = contratoProcessor.processar(abaContratos, response);
            Map<Long, Servico> servicosPorPlanilha = servicoProcessor.processar(abaServicos, response);
            relacionamentoProcessor.processar(abaRelacionamentos, contratosPorPlanilha, servicosPorPlanilha, response);

            return response;
        } catch (IOException e) {
            throw new BusinessException("Não foi possível ler a planilha enviada");
        }
    }
}
