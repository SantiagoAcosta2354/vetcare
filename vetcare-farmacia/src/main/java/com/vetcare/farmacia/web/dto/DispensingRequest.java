package com.vetcare.farmacia.web.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record DispensingRequest(
        @NotBlank(message = "La formula es obligatoria") String prescriptionId,
        String recordId,
        @NotBlank(message = "El paciente es obligatorio") String petId,
        String petName,
        String ownerId,
        @NotEmpty(message = "El despacho debe incluir al menos un medicamento") @Valid List<Item> items,
        @Size(max = 300) String notes) {

    public record Item(
            @NotBlank(message = "El medicamento es obligatorio") String medicationId,
            @Min(value = 1, message = "La cantidad debe ser mayor que cero") int quantity) {
    }
}
