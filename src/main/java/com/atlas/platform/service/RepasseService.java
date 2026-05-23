package com.atlas.platform.service;

import com.atlas.platform.dto.RepasseRequest;
import com.atlas.platform.dto.RepasseResponse;
import com.atlas.platform.exception.ResourceNotFoundException;
import com.atlas.platform.mapper.RepasseMapper;
import com.atlas.platform.model.Repasse;
import com.atlas.platform.repository.RepasseRepository;
import com.atlas.platform.util.TextoUtils;
import java.text.Normalizer;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RepasseService {

    private static final Comparator<Repasse> ORDENACAO_REPASSES = Comparator
            .comparingInt((Repasse repasse) -> prioridadePeso(repasse.getPrioridade()))
            .thenComparing(Repasse::getTitulo, String.CASE_INSENSITIVE_ORDER);

    private final RepasseRepository repository;
    private final AuditoriaService auditoriaService;
    private final RepasseMapper repasseMapper;

    public RepasseService(RepasseRepository repository, AuditoriaService auditoriaService, RepasseMapper repasseMapper) {
        this.repository = repository;
        this.auditoriaService = auditoriaService;
        this.repasseMapper = repasseMapper;
    }

    public List<RepasseResponse> listarAtivos() {
        return repository.findByAtivoTrue().stream()
                .sorted(ORDENACAO_REPASSES)
                .map(repasseMapper::toResponse)
                .toList();
    }

    @Transactional
    public RepasseResponse salvar(RepasseRequest request, Authentication authentication) {
        Repasse repasse = new Repasse();
        repasse.setCriadoEm(Instant.now());
        repasse.setAutor(nomeUsuario(authentication));
        aplicarRequest(repasse, request);

        if (Boolean.TRUE.equals(repasse.getFixado())) {
            desfixarOutros(null);
        }

        Repasse salvo = repository.save(repasse);
        auditoriaService.registrar("REPASSE", salvo.getId(), "CREATE_REPASSE", "Repasse criado: " + salvo.getTitulo());
        return repasseMapper.toResponse(salvo);
    }

    @Transactional
    public RepasseResponse atualizar(Long id, RepasseRequest request, Authentication authentication) {
        Repasse repasse = buscarEntidadePorId(id);
        repasse.setAutor(nomeUsuario(authentication));
        aplicarRequest(repasse, request);

        if (Boolean.TRUE.equals(repasse.getFixado())) {
            desfixarOutros(id);
        }

        Repasse salvo = repository.save(repasse);
        auditoriaService.registrar("REPASSE", salvo.getId(), "UPDATE_REPASSE", "Repasse atualizado: " + salvo.getTitulo());
        return repasseMapper.toResponse(salvo);
    }

    @Transactional
    public RepasseResponse ativar(Long id) {
        Repasse repasse = buscarEntidadePorId(id);
        repasse.setAtivo(true);
        Repasse salvo = repository.save(repasse);
        auditoriaService.registrar("REPASSE", salvo.getId(), "ATIVACAO", "Repasse ativado: " + salvo.getTitulo());
        return repasseMapper.toResponse(salvo);
    }

    @Transactional
    public RepasseResponse inativar(Long id) {
        Repasse repasse = buscarEntidadePorId(id);
        repasse.setAtivo(false);
        repasse.setFixado(false);
        Repasse salvo = repository.save(repasse);
        auditoriaService.registrar("REPASSE", salvo.getId(), "INATIVACAO", "Repasse inativado: " + salvo.getTitulo());
        return repasseMapper.toResponse(salvo);
    }

    @Transactional
    public RepasseResponse alternarFixado(Long id) {
        Repasse repasse = buscarEntidadePorId(id);
        boolean deveFixar = !Boolean.TRUE.equals(repasse.getFixado());

        if (deveFixar) {
            desfixarOutros(id);
        }

        repasse.setFixado(deveFixar);
        Repasse salvo = repository.save(repasse);
        auditoriaService.registrar("REPASSE", salvo.getId(), "FIXACAO", "Estado de fixação alterado: " + salvo.getTitulo());
        return repasseMapper.toResponse(salvo);
    }

    private Repasse buscarEntidadePorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Repasse não encontrado com id: " + id));
    }

    private void aplicarRequest(Repasse repasse, RepasseRequest request) {
        repasse.setTitulo(TextoUtils.normalizarObrigatorio(request.getTitulo()));
        repasse.setCategoria(TextoUtils.normalizarObrigatorio(request.getCategoria()));
        repasse.setConteudo(TextoUtils.normalizarObrigatorio(request.getConteudo()));
        repasse.setPrioridade(TextoUtils.normalizarObrigatorio(request.getPrioridade()));
        repasse.setAtivo(request.getAtivo() == null || request.getAtivo());
        repasse.setFixado(Boolean.TRUE.equals(request.getFixado()));
        repasse.setAnexoNome(TextoUtils.normalizarOpcional(request.getAnexoNome()));
    }

    private void desfixarOutros(Long repasseIdAtual) {
        repository.findAll().stream()
                .filter(item -> Boolean.TRUE.equals(item.getFixado()))
                .filter(item -> repasseIdAtual == null || !item.getId().equals(repasseIdAtual))
                .forEach(item -> item.setFixado(false));
    }

    private String nomeUsuario(Authentication authentication) {
        return authentication == null ? "ATLAS" : authentication.getName();
    }

    private static int prioridadePeso(String prioridade) {
        if (prioridade == null) {
            return Integer.MAX_VALUE;
        }

        return switch (normalizarPrioridade(prioridade)) {
            case "alta" -> 0;
            case "media" -> 1;
            case "baixa" -> 2;
            default -> 3;
        };
    }

    private static String normalizarPrioridade(String prioridade) {
        String valor = Normalizer.normalize(prioridade.trim().toLowerCase(), Normalizer.Form.NFD);
        return valor.replaceAll("\\p{M}", "");
    }

}
