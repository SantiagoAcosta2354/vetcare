package com.vetcare.pacientes.web.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AppointmentRequest(
        @NotBlank(message = "La sede es obligatoria") String clinicId,
        @NotBlank(message = "La mascota es obligatoria") String petId,
        @NotBlank(message = "El profesional es obligatorio") String professionalId,
        @NotBlank(message = "El nombre del profesional es obligatorio") String professionalName,
        @NotNull(message = "La fecha es obligatoria") @FutureOrPresent(message = "No se agenda en fechas pasadas") LocalDate date,
        @NotNull(message = "La hora de inicio es obligatoria") LocalTime startTime,
        @NotBlank(message = "El motivo de la consulta es obligatorio") @Size(max = 200) String reason,
        @Size(max = 500) String notes) {
}
