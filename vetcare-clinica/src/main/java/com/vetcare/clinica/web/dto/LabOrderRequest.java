package com.vetcare.clinica.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LabOrderRequest(
        @NotBlank(message = "El examen es obligatorio") @Size(max = 160) String examName,
        @Size(max = 500) String notes) {
}
