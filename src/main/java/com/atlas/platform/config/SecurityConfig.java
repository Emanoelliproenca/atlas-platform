package com.atlas.platform.config;

import com.atlas.platform.security.BearerTokenAuthenticationFilter;
import com.atlas.platform.security.SecurityErrorResponseWriter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableConfigurationProperties({SecurityProperties.class, SoftwareSyncProperties.class})
public class SecurityConfig {

    private static final String SESSION_EXPIRED_MESSAGE =
            "Sessão expirada ou credenciais inválidas. Entre novamente para continuar.";
    private static final String ACCESS_DENIED_MESSAGE =
            "Seu perfil não tem permissão para executar esta ação.";

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter,
            SecurityErrorResponseWriter securityErrorResponseWriter
    ) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/logout").authenticated()
                        .requestMatchers(HttpMethod.GET, "/auditoria/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/**").hasAnyRole("ADMIN", "VISUALIZADOR")
                        .requestMatchers(HttpMethod.POST, "/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(bearerTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                securityErrorResponseWriter.write(request, response, HttpStatus.UNAUTHORIZED, SESSION_EXPIRED_MESSAGE))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                securityErrorResponseWriter.write(request, response, HttpStatus.FORBIDDEN, ACCESS_DENIED_MESSAGE))
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(SecurityProperties properties, PasswordEncoder passwordEncoder) {
        validateConfiguredUser("admin", properties.getAdmin());
        validateConfiguredUser("viewer", properties.getViewer());

        return new InMemoryUserDetailsManager(
                User.withUsername(properties.getAdmin().getUsername())
                        .password(passwordEncoder.encode(properties.getAdmin().getPassword()))
                        .roles("ADMIN")
                        .build(),
                User.withUsername(properties.getViewer().getUsername())
                        .password(passwordEncoder.encode(properties.getViewer().getPassword()))
                        .roles("VISUALIZADOR")
                        .build()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void validateConfiguredUser(String profile, SecurityProperties.User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()
                || user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalStateException("Configure app.security." + profile + ".username and app.security." + profile + ".password.");
        }
    }
}
