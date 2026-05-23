package com.atlas.platform.service;

import com.atlas.platform.dto.AuditoriaEventoResponse;
import com.atlas.platform.model.AuditoriaEvento;
import com.atlas.platform.repository.AuditoriaEventoRepository;
import java.time.Instant;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuditoriaService {

    private final AuditoriaEventoRepository repository;

    public AuditoriaService(AuditoriaEventoRepository repository) {
        this.repository = repository;
    }

    public List<AuditoriaEventoResponse> listarRecentes() {
        return repository.findTop100ByOrderByTimestampDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void registrar(String entidade, Long entidadeId, String acao, String detalhes) {
        registrar(entidade, entidadeId, acao, detalhes, null, null);
    }

    @Transactional
    public void registrar(String entidade, Long entidadeId, String acao, String detalhes, String usuario, String ip) {
        AuditoriaEvento evento = new AuditoriaEvento();
        evento.setEntidade(valorOuPadrao(entidade, "SISTEMA"));
        evento.setEntidadeId(entidadeId == null ? 0L : entidadeId);
        evento.setAcao(acao);
        evento.setDetalhes(valorOuPadrao(detalhes, "Evento registrado"));
        evento.setUsuario(valorOuPadrao(usuario, usuarioAtual()));
        evento.setIp(ip);
        evento.setTimestamp(Instant.now());
        repository.save(evento);
    }

    @Transactional
    public void registrar(String entidade, Long entidadeId, String acao, String detalhes, HttpServletRequest request) {
        registrar(entidade, entidadeId, acao, detalhes, null, extrairIp(request));
    }

    private String usuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "ATLAS";
        }
        return authentication.getName();
    }

    public String extrairIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String valorOuPadrao(String valor, String padrao) {
        return valor == null || valor.isBlank() ? padrao : valor;
    }

    private AuditoriaEventoResponse toResponse(AuditoriaEvento evento) {
        return new AuditoriaEventoResponse(
                evento.getId(),
                evento.getEntidade(),
                evento.getEntidadeId(),
                evento.getAcao(),
                evento.getUsuario(),
                evento.getDetalhes(),
                evento.getIp(),
                evento.getTimestamp()
        );
    }
}
