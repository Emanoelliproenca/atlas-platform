package com.atlas.platform.service;

import com.atlas.platform.exception.BusinessException;
import java.util.Objects;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImportacaoPlanilhaWorkbookReader {

    private final DataFormatter dataFormatter = new DataFormatter();

    public void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new BusinessException("Envie uma planilha .xlsx para importação");
        }

        String nome = arquivo.getOriginalFilename();
        if (nome == null || !nome.toLowerCase().endsWith(".xlsx")) {
            throw new BusinessException("A importação aceita apenas arquivos .xlsx");
        }
    }

    public Sheet obterAbaObrigatoria(Workbook workbook, String nomeAba) {
        Sheet sheet = workbook.getSheet(nomeAba);
        if (sheet == null) {
            throw new BusinessException("A planilha não possui a aba obrigatória: " + nomeAba);
        }
        return sheet;
    }

    public void validarCabecalho(Sheet sheet, String... colunasEsperadas) {
        Row header = sheet.getRow(0);
        if (header == null) {
            throw new BusinessException("A aba " + sheet.getSheetName() + " está vazia");
        }

        for (int i = 0; i < colunasEsperadas.length; i++) {
            String valor = getString(header, i);
            if (!Objects.equals(colunasEsperadas[i], valor)) {
                throw new BusinessException(
                        "Cabeçalho inválido na aba " + sheet.getSheetName() + ", coluna " + (i + 1) + ". Esperado: " + colunasEsperadas[i]
                );
            }
        }
    }

    public boolean linhaVazia(Row row) {
        if (row == null) {
            return true;
        }

        for (Cell cell : row) {
            if (!dataFormatter.formatCellValue(cell).isBlank()) {
                return false;
            }
        }

        return true;
    }

    public Long getLong(Row row, int index, String campo, int linha) {
        String valor = getString(row, index);
        if (valor.isBlank()) {
            throw new BusinessException("Campo " + campo + " vazio na linha " + (linha + 1));
        }

        try {
            return Long.valueOf(valor);
        } catch (NumberFormatException exception) {
            throw new BusinessException("Campo " + campo + " inválido na linha " + (linha + 1));
        }
    }

    public String getString(Row row, int index) {
        Cell cell = row.getCell(index);
        return cell == null ? "" : dataFormatter.formatCellValue(cell).trim();
    }
}
