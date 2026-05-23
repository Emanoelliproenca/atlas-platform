package com.atlas.platform.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>(List.of(
            "http://localhost:4200",
            "http://localhost:4300",
            "http://localhost:4311",
            "http://localhost:4312",
            "http://127.0.0.1:4200",
            "http://127.0.0.1:4300",
            "http://127.0.0.1:4311",
            "http://127.0.0.1:4312"
    ));

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins == null ? List.of() : allowedOrigins;
    }
}
