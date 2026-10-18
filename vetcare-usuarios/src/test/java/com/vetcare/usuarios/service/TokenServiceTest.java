package com.vetcare.usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.vetcare.usuarios.domain.Role;
import com.vetcare.usuarios.domain.User;
import com.vetcare.usuarios.web.dto.TokenResponse;

/** El token debe llevar los roles y el identificador que los otros servicios leen. */
class TokenServiceTest {

    private static final String SECRET = "vetcare-clave-de-desarrollo-cambiar-en-produccion-2026";

    private TokenService tokenService;
    private JwtDecoder decoder;

    @BeforeEach
    void setUp() {
        SecretKey key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();

        tokenService = new TokenService(encoder);
        ReflectionTestUtils.setField(tokenService, "issuer", "vetcare-usuarios");
        ReflectionTestUtils.setField(tokenService, "expirationMinutes", 480L);
    }

    @Test
    @DisplayName("El token incluye sub, correo, nombre y roles")
    void tokenLlevaIdentidadYRoles() {
        User user = User.create("Dra. Laura Gomez", "veterinario@vetcare.co", null, "hash",
                Set.of(Role.VETERINARIO), "sistema");
        user.setId("60f1");

        TokenResponse response = tokenService.issue(user);
        Jwt jwt = decoder.decode(response.accessToken());

        assertThat(jwt.getSubject()).isEqualTo("60f1");
        assertThat(jwt.getClaimAsString("email")).isEqualTo("veterinario@vetcare.co");
        assertThat(jwt.getClaimAsString("name")).isEqualTo("Dra. Laura Gomez");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("VETERINARIO");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("vetcare-usuarios");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Un token firmado con otro secreto no se puede validar")
    void tokenDeOtroSecretoSeRechaza() {
        User user = User.create("Admin", "admin@vetcare.co", null, "hash", Set.of(Role.ADMIN), "sistema");
        user.setId("60f2");
        String token = tokenService.issue(user).accessToken();

        SecretKey otraClave = new SecretKeySpec(
                "otro-secreto-distinto-de-32-caracteres-para-probar".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtDecoder otroDecoder = NimbusJwtDecoder.withSecretKey(otraClave)
                .macAlgorithm(MacAlgorithm.HS256).build();

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> otroDecoder.decode(token))
                .isInstanceOf(org.springframework.security.oauth2.jwt.JwtException.class);
    }
}
