package com.atlas.platform.controller;

import com.atlas.platform.dto.AuthLoginRequest;
import com.atlas.platform.dto.AuthLoginResponse;
import com.atlas.platform.dto.SessaoUsuarioResponse;
import com.atlas.platform.exception.BusinessException;
import com.atlas.platform.response.ApiResponse;
import com.atlas.platform.response.ApiResponses;
import com.atlas.platform.security.SessionTokenService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final SessionTokenService sessionTokenService;
    private final com.atlas.platform.service.AuditoriaService auditoriaService;

    public AuthController(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            SessionTokenService sessionTokenService,
            com.atlas.platform.service.AuditoriaService auditoriaService
    ) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.sessionTokenService = sessionTokenService;
        this.auditoriaService = auditoriaService;
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica usuario e retorna token Bearer", security = {})
    public ApiResponse<AuthLoginResponse> login(@RequestBody @Valid AuthLoginRequest request, HttpServletRequest servletRequest) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String password = request.getPassword() == null ? "" : request.getPassword();

        if (username.isBlank() || password.isBlank()) {
            auditoriaService.registrar("AUTH", null, "LOGIN_FAILURE", "Tentativa de login com credenciais incompletas", username, auditoriaService.extrairIp(servletRequest));
            throw new BusinessException("Informe usuario e senha para entrar");
        }

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(username);
        } catch (Exception exception) {
            auditoriaService.registrar("AUTH", null, "LOGIN_FAILURE", "Usuário não encontrado", username, auditoriaService.extrairIp(servletRequest));
            throw credenciaisInvalidas();
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            auditoriaService.registrar("AUTH", null, "LOGIN_FAILURE", "Senha inválida", username, auditoriaService.extrairIp(servletRequest));
            throw credenciaisInvalidas();
        }

        String token = sessionTokenService.criar(userDetails);
        auditoriaService.registrar("AUTH", null, "LOGIN_SUCCESS", "Sessão iniciada com sucesso", userDetails.getUsername(), auditoriaService.extrairIp(servletRequest));
        return ApiResponses.success(
                "Sessao iniciada com sucesso",
                new AuthLoginResponse(
                        token,
                        userDetails.getUsername(),
                        extrairRoles(userDetails.getAuthorities()),
                        sessionTokenService.expiraEm(token).map(java.time.Instant::toString).orElse(null)
                )
        );
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String token = sessionTokenService.extrairBearerToken(request.getHeader("Authorization"));
        sessionTokenService.invalidar(token);
        auditoriaService.registrar("AUTH", null, "LOGOUT", "Sessão encerrada com sucesso", request);

        return ApiResponses.successWithoutData("Sessao encerrada com sucesso");
    }

    @GetMapping("/me")
    public ApiResponse<SessaoUsuarioResponse> me(Authentication authentication, HttpServletRequest request) {
        String token = sessionTokenService.extrairBearerToken(request.getHeader("Authorization"));
        return ApiResponses.success(
                "Sessão carregada com sucesso",
                new SessaoUsuarioResponse(
                        authentication.getName(),
                        extrairRoles(authentication.getAuthorities()),
                        sessionTokenService.expiraEm(token).map(java.time.Instant::toString).orElse(null)
                )
        );
    }

    private List<String> extrairRoles(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    private ResponseStatusException credenciaisInvalidas() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas");
    }
}
