package com.vetcare.clinica.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OpenRecordRequest(
        @NotBlank(message = "La cita es obligatoria") String appointmentId,
        @NotBlank(message = "El profesional es obligatorio") String professionalId,
        @NotBlank(message = "El nombre del profesional es obligatorio") String professionalName,
        @Size(max = 200) String reason) {
}
