package com.vetcare.farmacia.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Farmacia escribe su propio inventario. El veterinario solo consulta el
 * catalogo para saber si el medicamento existe antes de formular; no despacha ni
 * recibe mercancia.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    public SecurityConfig(JwtAuthenticationConverter jwtAuthenticationConverter) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/medications/**", "/api/v1/purchases/**")
                    .hasAnyRole("ADMIN", "FARMACIA")
                .requestMatchers(HttpMethod.PUT, "/api/v1/medications/**").hasAnyRole("ADMIN", "FARMACIA")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/medications/**").hasAnyRole("ADMIN", "FARMACIA")
                .requestMatchers(HttpMethod.POST, "/api/v1/dispensings/**").hasAnyRole("ADMIN", "FARMACIA")
                .requestMatchers("/api/v1/alerts/**").hasAnyRole("ADMIN", "FARMACIA")
                // Recepcion necesita ver lo despachado para poder facturar los productos.
                .requestMatchers(HttpMethod.GET, "/api/v1/dispensings/**")
                    .hasAnyRole("ADMIN", "FARMACIA", "VETERINARIO", "RECEPCION")
                .requestMatchers("/api/v1/medications/**")
                    .hasAnyRole("ADMIN", "FARMACIA", "VETERINARIO")
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                .authenticationEntryPoint((request, response, ex) -> writeError(response,
                        HttpServletResponse.SC_UNAUTHORIZED, "Se requiere un token valido para realizar esta operacion"))
                .accessDeniedHandler((request, response, ex) -> writeError(response,
                        HttpServletResponse.SC_FORBIDDEN, "El usuario autenticado no tiene permisos para esta operacion")));
        return http.build();
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"error\":\"" + message + "\"}");
    }
}
