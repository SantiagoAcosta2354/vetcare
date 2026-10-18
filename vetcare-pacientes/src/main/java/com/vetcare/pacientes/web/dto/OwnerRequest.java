package com.vetcare.pacientes.web.dto;

import com.vetcare.pacientes.domain.DocumentType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OwnerRequest(
        @NotNull(message = "El tipo de documento es obligatorio") DocumentType documentType,
        @NotBlank(message = "El documento es obligatorio") @Size(max = 30) String document,
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 120) String fullName,
        @Pattern(regexp = "^[0-9+()\\-\\s]{7,25}$", message = "El telefono no tiene un formato valido") String phone,
        @Email(message = "El correo no tiene un formato valido") String email,
        @Size(max = 200) String address,
        String userId) {
}
