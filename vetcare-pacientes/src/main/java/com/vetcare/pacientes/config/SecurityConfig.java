package com.vetcare.pacientes.config;

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
 * Permisos de pacientes y agenda.
 *
 * Recepcion escribe duenos, mascotas y citas. El veterinario y el auxiliar de
 * laboratorio consultan la agenda y la ficha del paciente, pero no registran
 * duenos. El dueno solo entra por /api/v1/portal, donde el filtro se toma del
 * token y no de la URL.
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
                .requestMatchers("/api/v1/portal/**").hasRole("DUENO")
                .requestMatchers(HttpMethod.POST, "/api/v1/clinics/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/owners/**", "/api/v1/pets/**")
                    .hasAnyRole("ADMIN", "RECEPCION")
                .requestMatchers(HttpMethod.PUT, "/api/v1/owners/**", "/api/v1/pets/**")
                    .hasAnyRole("ADMIN", "RECEPCION")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/owners/**", "/api/v1/pets/**")
                    .hasAnyRole("ADMIN", "RECEPCION")
                .requestMatchers("/api/v1/owners/**", "/api/v1/pets/**", "/api/v1/clinics/**",
                        "/api/v1/appointments/**")
                    .hasAnyRole("ADMIN", "RECEPCION", "VETERINARIO", "LABORATORIO")
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
