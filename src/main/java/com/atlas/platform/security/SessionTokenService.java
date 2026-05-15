package com.atlas.platform.security;

import com.atlas.platform.config.SecurityProperties;
import com.atlas.platform.model.SessaoAutenticacao;
import com.atlas.platform.repository.SessaoAutenticacaoRepository;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionTokenService {

    private final SessaoAutenticacaoRepository sessaoAutenticacaoRepository;
    private final Duration sessionTtl;

    public SessionTokenService(
            SecurityProperties securityProperties,
            SessaoAutenticacaoRepository sessaoAutenticacaoRepository
    ) {
        long configuredMinutes = Math.max(1, securityProperties.getSessionTtlMinutes());
        this.sessionTtl = Duration.ofMinutes(configuredMinutes);
        this.sessaoAutenticacaoRepository = sessaoAutenticacaoRepository;
    }

    @Transactional
    public String criar(UserDetails userDetails) {
        String token = UUID.randomUUID().toString();
        Instant agora = Instant.now();

        limparExpiradas();
        sessaoAutenticacaoRepository.save(criarSessao(userDetails, token, agora));

        return token;
    }

    @Transactional(readOnly = true)
    public Optional<Authentication> autenticar(String token) {
        Optional<SessaoAutenticacao> sessao = buscarSessaoValida(token);
        if (sessao.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(UsernamePasswordAuthenticationToken.authenticated(
                sessao.get().getUsername(),
                null,
                authorities(sessao.get())
        ));
    }

    @Transactional(readOnly = true)
    public Optional<Instant> expiraEm(String token) {
        return buscarSessaoValida(token).map(SessaoAutenticacao::getExpiraEm);
    }

    @Transactional
    public void invalidar(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        sessaoAutenticacaoRepository.findByTokenHashAndInvalidaFalse(hashToken(token))
                .ifPresent(sessao -> {
                    sessao.setInvalida(true);
                    sessaoAutenticacaoRepository.save(sessao);
                });

        limparExpiradas();
    }

    public String extrairBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        return authorizationHeader.substring(7).trim();
    }

    @Transactional
    public void limparExpiradas() {
        sessaoAutenticacaoRepository.deleteByExpiraEmBeforeOrInvalidaTrue(Instant.now());
    }

    private Optional<SessaoAutenticacao> buscarSessaoValida(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        Optional<SessaoAutenticacao> sessao = sessaoAutenticacaoRepository.findByTokenHashAndInvalidaFalse(hashToken(token));
        if (sessao.isEmpty()) {
            return Optional.empty();
        }

        if (sessao.get().getExpiraEm().isBefore(Instant.now())) {
            invalidar(token);
            return Optional.empty();
        }

        return sessao;
    }

    private SessaoAutenticacao criarSessao(UserDetails userDetails, String token, Instant agora) {
        SessaoAutenticacao sessao = new SessaoAutenticacao();
        sessao.setTokenHash(hashToken(token));
        sessao.setUsername(userDetails.getUsername());
        sessao.setRoles(serializarRoles(userDetails));
        sessao.setCriadaEm(agora);
        sessao.setExpiraEm(agora.plus(sessionTtl));
        sessao.setInvalida(false);
        return sessao;
    }

    private String serializarRoles(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.joining(","));
    }

    private java.util.List<SimpleGrantedAuthority> authorities(SessaoAutenticacao sessao) {
        return java.util.Arrays.stream(sessao.getRoles().split(","))
                .filter(role -> !role.isBlank())
                .map(String::trim)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponivel", exception);
        }
    }
}
