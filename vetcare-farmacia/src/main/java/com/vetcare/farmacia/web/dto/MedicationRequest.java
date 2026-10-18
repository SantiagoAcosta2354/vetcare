package com.vetcare.farmacia.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MedicationRequest(
        @NotBlank(message = "El nombre del medicamento es obligatorio") @Size(max = 120) String name,
        @Size(max = 120) String activeIngredient,
        @Size(max = 80) String presentation,
        @Size(max = 30) String unit,
        @Min(value = 0, message = "El stock minimo no puede ser negativo") Integer stockMin) {
}
