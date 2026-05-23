package com.atlas.platform.service;

import com.atlas.platform.dto.SoftwareArtefatoResponse;
import com.atlas.platform.exception.ResourceNotFoundException;
import com.atlas.platform.model.Servico;
import com.atlas.platform.model.SoftwareArtefato;
import com.atlas.platform.repository.ServicoRepository;
import com.atlas.platform.repository.SoftwareArtefatoRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class SoftwareArtefatoService {

    private final SoftwareArtefatoRepository repository;
    private final ServicoRepository servicoRepository;
    private final AuditoriaService auditoriaService;
    private final ArquivoUploadService arquivoUploadService;

    public SoftwareArtefatoService(
            SoftwareArtefatoRepository repository,
            ServicoRepository servicoRepository,
            AuditoriaService auditoriaService,
            ArquivoUploadService arquivoUploadService
    ) {
        this.repository = repository;
        this.servicoRepository = servicoRepository;
        this.auditoriaService = auditoriaService;
        this.arquivoUploadService = arquivoUploadService;
    }

    public List<SoftwareArtefatoResponse> listarPorServico(Long servicoId) {
        return repository.findByServicoIdOrderByCriadoEmDesc(servicoId).stream()
                .map(this::toResponse)
                .toList();
    }

    public SoftwareArtefato buscarArtefatoAtual(Long servicoId) {
        return repository.findFirstByServicoIdAndAtivoTrueOrderByCriadoEmDesc(servicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum artefato disponível para este sistema."));
    }

    @Transactional
    public SoftwareArtefatoResponse salvar(Long servicoId, MultipartFile arquivo, String versao) {
        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado com id: " + servicoId));
        ArquivoUploadData upload = arquivoUploadService.preparar(
                arquivo,
                "artefato",
                "Selecione um artefato para enviar.",
                "Não foi possível ler o artefato enviado."
        );

        SoftwareArtefato artefato = new SoftwareArtefato();
        artefato.setServico(servico);
        artefato.setNome(upload.nome());
        artefato.setTipoConteudo(upload.tipoConteudo());
        artefato.setTamanho(upload.tamanho());
        artefato.setVersao(normalizarOpcional(versao));
        artefato.setAtivo(true);
        artefato.setCriadoEm(LocalDateTime.now());
        artefato.setConteudo(upload.conteudo());

        SoftwareArtefato salvo = repository.save(artefato);
        auditoriaService.registrar("SOFTWARE_ARTEFATO", salvo.getId(), "FILE_UPLOAD", "Artefato enviado para " + servico.getNome());
        return toResponse(salvo);
    }

    private SoftwareArtefatoResponse toResponse(SoftwareArtefato artefato) {
        return new SoftwareArtefatoResponse(
                artefato.getId(),
                artefato.getNome(),
                artefato.getTipoConteudo(),
                artefato.getTamanho(),
                artefato.getVersao(),
                artefato.getAtivo(),
                artefato.getCriadoEm()
        );
    }

    private String normalizarOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
