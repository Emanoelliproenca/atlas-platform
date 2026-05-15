package com.atlas.platform.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class ApiResponses {

    private ApiResponses() {
    }

    public static <T> ApiResponse<T> success(String mensagem, T dados) {
        return new ApiResponse<>(true, mensagem, dados);
    }

    public static ApiResponse<Void> successWithoutData(String mensagem) {
        return success(mensagem, null);
    }

    public static <T> ApiResponse<T> error(String mensagem, T dados) {
        return new ApiResponse<>(false, mensagem, dados);
    }

    public static ApiResponse<Void> error(String mensagem) {
        return error(mensagem, null);
    }

    public static ResponseEntity<ApiResponse<Void>> errorResponse(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(error(mensagem));
    }
}
