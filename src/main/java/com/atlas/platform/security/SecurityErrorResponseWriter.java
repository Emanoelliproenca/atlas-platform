package com.atlas.platform.security;

import com.atlas.platform.response.ApiErrorResponse;
import com.atlas.platform.response.ApiResponses;
import com.atlas.platform.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper;
    private final AuditoriaService auditoriaService;

    public SecurityErrorResponseWriter(ObjectMapper objectMapper, AuditoriaService auditoriaService) {
        this.objectMapper = objectMapper;
        this.auditoriaService = auditoriaService;
    }

    public void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String message) throws IOException {
        if (status == HttpStatus.FORBIDDEN) {
            auditoriaService.registrar("SEGURANCA", null, "ACCESS_DENIED", message, usuarioAtual(), auditoriaService.extrairIp(request));
        }

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiErrorResponse error = ApiResponses.structuredError(status, message, request.getRequestURI());
        objectMapper.writeValue(response.getWriter(), error);
    }

    private String usuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "ANONIMO" : authentication.getName();
    }
}
