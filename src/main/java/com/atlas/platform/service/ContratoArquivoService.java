package com.atlas.platform.service;

import com.atlas.platform.dto.ContratoArquivoResponse;
import com.atlas.platform.exception.BusinessException;
import com.atlas.platform.exception.ResourceNotFoundException;
import com.atlas.platform.model.Contrato;
import com.atlas.platform.model.ContratoArquivo;
import com.atlas.platform.repository.ContratoArquivoRepository;
import com.atlas.platform.repository.ContratoRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class ContratoArquivoService {

    private static final String TIPO_CONTEUDO_PADRAO = "application/octet-stream";

    private final ContratoArquivoRepository repository;
    private final ContratoRepository contratoRepository;

    public ContratoArquivoService(ContratoArquivoRepository repository, ContratoRepository contratoRepository) {
        this.repository = repository;
        this.contratoRepository = contratoRepository;
    }

    public List<ContratoArquivoResponse> listarPorContrato(Long contratoId) {
        return repository.findByContratoIdOrderByCriadoEmDesc(contratoId).stream()
                .map(this::toResponse)
                .toList();
    }

    public ContratoArquivo buscarArquivo(Long contratoId, Long arquivoId) {
        ContratoArquivo arquivo = repository.findById(arquivoId)
                .orElseThrow(() -> new ResourceNotFoundException("Arquivo não encontrado com id: " + arquivoId));

        if (!arquivo.getContrato().getId().equals(contratoId)) {
            throw new ResourceNotFoundException("Arquivo não encontrado para este contrato.");
        }

        return arquivo;
    }

    @Transactional
    public ContratoArquivoResponse salvar(Long contratoId, MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new BusinessException("Selecione um arquivo para enviar.");
        }

        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado com id: " + contratoId));

        ContratoArquivo entidade = new ContratoArquivo();
        entidade.setContrato(contrato);
        entidade.setNome(normalizarNome(arquivo.getOriginalFilename()));
        entidade.setTipoConteudo(normalizarTipoConteudo(arquivo.getContentType()));
        entidade.setTamanho(arquivo.getSize());
        entidade.setCriadoEm(LocalDateTime.now());

        try {
            entidade.setConteudo(arquivo.getBytes());
        } catch (IOException ex) {
            throw new BusinessException("Não foi possível ler o arquivo enviado.");
        }

        return toResponse(repository.save(entidade));
    }

    private ContratoArquivoResponse toResponse(ContratoArquivo arquivo) {
        return new ContratoArquivoResponse(
                arquivo.getId(),
                arquivo.getNome(),
                arquivo.getTipoConteudo(),
                arquivo.getTamanho(),
                arquivo.getCriadoEm()
        );
    }

    private String normalizarNome(String nome) {
        String nomeNormalizado = nome == null ? "" : nome.trim();
        if (nomeNormalizado.isBlank()) {
            return "arquivo";
        }

        return nomeNormalizado.replaceAll("[\\\\/]+", "_");
    }

    private String normalizarTipoConteudo(String tipoConteudo) {
        return tipoConteudo == null || tipoConteudo.isBlank()
                ? TIPO_CONTEUDO_PADRAO
                : tipoConteudo;
    }
}
