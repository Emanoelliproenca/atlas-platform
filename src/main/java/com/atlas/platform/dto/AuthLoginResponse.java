package com.atlas.platform.dto;

import java.util.List;

public class AuthLoginResponse {

    private final String token;
    private final String username;
    private final List<String> roles;
    private final String expiraEm;

    public AuthLoginResponse(String token, String username, List<String> roles, String expiraEm) {
        this.token = token;
        this.username = username;
        this.roles = roles;
        this.expiraEm = expiraEm;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getExpiraEm() {
        return expiraEm;
    }
}
