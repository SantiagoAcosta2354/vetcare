package com.vetcare.pacientes.web.dto;

import com.vetcare.pacientes.domain.AppointmentStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StatusChangeRequest(
        @NotNull(message = "El estado destino es obligatorio") AppointmentStatus status,
        @Size(max = 200) String reason) {
}
