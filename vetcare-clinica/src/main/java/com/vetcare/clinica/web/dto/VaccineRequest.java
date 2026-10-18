package com.vetcare.clinica.web.dto;

import com.vetcare.clinica.domain.Species;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VaccineRequest(
        @NotBlank(message = "El nombre de la vacuna es obligatorio") @Size(max = 120) String name,
        Species species,
        @Min(value = 1, message = "Los dias para la proxima dosis deben ser mayores que cero") int nextDoseDays) {
}
