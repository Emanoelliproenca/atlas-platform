package com.atlas.platform.service;

import com.atlas.platform.dto.ContratoArquivoResponse;
import com.atlas.platform.exception.ResourceNotFoundException;
import com.atlas.platform.model.Contrato;
import com.atlas.platform.model.ContratoArquivo;
import com.atlas.platform.repository.ContratoArquivoRepository;
import com.atlas.platform.repository.ContratoRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class ContratoArquivoService {

    private final ContratoArquivoRepository repository;
    private final ContratoRepository contratoRepository;
    private final AuditoriaService auditoriaService;
    private final ArquivoUploadService arquivoUploadService;

    public ContratoArquivoService(
            ContratoArquivoRepository repository,
            ContratoRepository contratoRepository,
            AuditoriaService auditoriaService,
            ArquivoUploadService arquivoUploadService
    ) {
        this.repository = repository;
        this.contratoRepository = contratoRepository;
        this.auditoriaService = auditoriaService;
        this.arquivoUploadService = arquivoUploadService;
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
        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado com id: " + contratoId));
        ArquivoUploadData upload = arquivoUploadService.preparar(
                arquivo,
                "arquivo",
                "Selecione um arquivo para enviar.",
                "Não foi possível ler o arquivo enviado."
        );

        ContratoArquivo entidade = new ContratoArquivo();
        entidade.setContrato(contrato);
        entidade.setNome(upload.nome());
        entidade.setTipoConteudo(upload.tipoConteudo());
        entidade.setTamanho(upload.tamanho());
        entidade.setCriadoEm(LocalDateTime.now());
        entidade.setConteudo(upload.conteudo());

        ContratoArquivo salvo = repository.save(entidade);
        auditoriaService.registrar("CONTRATO_ARQUIVO", salvo.getId(), "FILE_UPLOAD",
                "Arquivo enviado para contrato " + contrato.getNome() + ": " + salvo.getNome());
        return toResponse(salvo);
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

}
