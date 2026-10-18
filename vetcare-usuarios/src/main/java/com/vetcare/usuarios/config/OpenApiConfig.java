package com.vetcare.usuarios.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/** Contrato OpenAPI del servicio, con el esquema de token que usan los cinco modulos. */
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    @Bean
    OpenAPI vetcareOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("VetCare - Usuarios")
                        .version("1.0.0")
                        .description("Cuentas, perfiles, autenticacion y emision de tokens de VetCare")
                        .contact(new Contact().name("Equipo VetCare")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Token emitido por POST /api/v1/auth/login")));
    }
}
