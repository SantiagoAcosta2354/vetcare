package com.vetcare.usuarios.exception;

/**
 * Credenciales invalidas. Se traduce a HTTP 401 con un mensaje generico: no
 * distingue entre correo inexistente y contrasena incorrecta.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
