package com.vetcare.usuarios.web.dto;

/**
 * Respuesta del inicio de sesion. La capa web guarda el token en la sesion y lo
 * reenvia en cada llamada: no guarda contrasenas ni consulta este servicio de
 * nuevo.
 */
public record TokenResponse(String accessToken, String tokenType, long expiresIn, UserResponse user) {

    public static TokenResponse bearer(String token, long expiresIn, UserResponse user) {
        return new TokenResponse(token, "Bearer", expiresIn, user);
    }
}
