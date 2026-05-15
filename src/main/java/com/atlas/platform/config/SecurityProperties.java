package com.atlas.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private final User admin = new User();
    private final User viewer = new User();
    private long sessionTtlMinutes = 120;

    public User getAdmin() {
        return admin;
    }

    public User getViewer() {
        return viewer;
    }

    public long getSessionTtlMinutes() {
        return sessionTtlMinutes;
    }

    public void setSessionTtlMinutes(long sessionTtlMinutes) {
        this.sessionTtlMinutes = sessionTtlMinutes;
    }

    public static class User {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
