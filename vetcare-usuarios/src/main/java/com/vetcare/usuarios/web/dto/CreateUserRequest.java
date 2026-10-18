package com.vetcare.usuarios.web.dto;

import java.util.Set;

import com.vetcare.usuarios.domain.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Creacion de una cuenta desde la administracion. */
public record CreateUserRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 120) String fullName,
        @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no tiene un formato valido") @Size(max = 160) String email,
        @Pattern(regexp = "^[0-9+()\\-\\s]{0,25}$", message = "El telefono tiene caracteres no validos") String phone,
        @NotBlank(message = "La contrasena es obligatoria") @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres") String password,
        @NotEmpty(message = "Debe asignar al menos un rol") Set<Role> roles) {
}
