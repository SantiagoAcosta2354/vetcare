package com.vetcare.usuarios.web.dto;

import java.time.Instant;
import java.util.List;

/** Cuerpo unico de error de todos los servicios de la plataforma. */
public record ApiError(Instant timestamp, int status, String error, String message, String path, List<String> details) {

    public static ApiError of(int status, String error, String message, String path, List<String> details) {
        return new ApiError(Instant.now(), status, error, message, path, details == null ? List.of() : details);
    }
}
