package com.vetcare.pacientes.web.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClinicRequest(
        @NotBlank(message = "El nombre de la sede es obligatorio") @Size(max = 120) String name,
        @Size(max = 200) String address,
        @Size(max = 30) String phone,
        LocalTime openingTime,
        LocalTime closingTime,
        @Min(value = 10, message = "La franja minima es de 10 minutos")
        @Max(value = 120, message = "La franja maxima es de 120 minutos") Integer slotMinutes) {
}
