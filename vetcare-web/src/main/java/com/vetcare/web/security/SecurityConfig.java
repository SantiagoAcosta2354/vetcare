package com.vetcare.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cada perfil entra solo a su area. La regla tambien vive en cada microservicio:
 * ocultar el enlace no protege la operacion, y por eso el servicio responde 403
 * si alguien llama la ruta directamente.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/error", "/actuator/health/**").permitAll()
                .requestMatchers("/admin/**", "/tablero/**").hasRole("ADMIN")
                .requestMatchers("/agenda/**", "/facturacion/**").hasAnyRole("ADMIN", "RECEPCION")
                .requestMatchers("/atencion/**").hasAnyRole("ADMIN", "VETERINARIO")
                .requestMatchers("/examenes/**").hasAnyRole("ADMIN", "LABORATORIO")
                .requestMatchers("/farmacia/**").hasAnyRole("ADMIN", "FARMACIA")
                .requestMatchers("/portal/**").hasRole("DUENO")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID"));
        return http.build();
    }
}
