package com.vetcare.clinica.web.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record PrescriptionRequest(
        @NotEmpty(message = "La formula debe incluir al menos un medicamento") @Valid List<Item> items,
        @Size(max = 500) String observations) {

    public record Item(
            @NotBlank(message = "El medicamento es obligatorio") String medicationId,
            @NotBlank(message = "La dosis es obligatoria") @Size(max = 80) String dose,
            @Size(max = 80) String frequency,
            @Min(value = 1, message = "La duracion debe ser al menos de un dia") Integer durationDays,
            @Min(value = 1, message = "La cantidad debe ser mayor que cero") int quantity) {
    }
}
