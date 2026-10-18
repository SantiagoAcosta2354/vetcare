package com.vetcare.usuarios.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "La contrasena actual es obligatoria") String currentPassword,
        @NotBlank(message = "La contrasena nueva es obligatoria") @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres") String newPassword) {
}
