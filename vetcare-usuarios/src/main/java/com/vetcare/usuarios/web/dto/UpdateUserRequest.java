package com.vetcare.usuarios.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos de presentacion de una cuenta. El correo no se cambia aqui. */
public record UpdateUserRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 120) String fullName,
        @Pattern(regexp = "^[0-9+()\\-\\s]{0,25}$", message = "El telefono tiene caracteres no validos") String phone) {
}
