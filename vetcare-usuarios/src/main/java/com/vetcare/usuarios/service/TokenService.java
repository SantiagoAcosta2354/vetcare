package com.vetcare.usuarios.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.vetcare.usuarios.domain.Role;
import com.vetcare.usuarios.domain.User;
import com.vetcare.usuarios.web.dto.TokenResponse;
import com.vetcare.usuarios.web.dto.UserResponse;

/**
 * Emision de tokens. Es la unica responsabilidad que ningun otro servicio puede
 * asumir: los demas solo validan la firma con el secreto compartido, de modo que
 * no necesitan consultar este servicio en cada peticion.
 *
 * El token lleva el identificador en "sub", los roles en "roles" y datos de
 * presentacion (correo y nombre) para que la capa web no tenga que hacer
 * consultas adicionales para pintar el encabezado.
 */
@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;

    @Value("${vetcare.security.jwt.issuer}")
    private String issuer;

    @Value("${vetcare.security.jwt.expiration-minutes}")
    private long expirationMinutes;

    public TokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public TokenResponse issue(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(expirationMinutes));
        List<String> roles = user.getRoles().stream().map(Role::name).sorted().toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(user.getId())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("email", user.getEmail())
                .claim("name", user.getFullName())
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return TokenResponse.bearer(token, Duration.between(now, expiresAt).toSeconds(), UserResponse.from(user));
    }
}
