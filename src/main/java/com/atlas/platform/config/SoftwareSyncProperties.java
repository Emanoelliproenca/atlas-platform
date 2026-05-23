package com.atlas.platform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.software-sync")
public class SoftwareSyncProperties {

    private String manifestUrl;
}
