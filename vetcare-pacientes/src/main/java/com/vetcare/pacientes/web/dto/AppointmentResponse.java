package com.vetcare.pacientes.web.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import com.vetcare.pacientes.domain.Appointment;
import com.vetcare.pacientes.domain.AppointmentStatus;
import com.vetcare.pacientes.domain.Species;

public record AppointmentResponse(
        String id,
        String clinicId,
        String clinicName,
        String petId,
        String petName,
        Species petSpecies,
        String ownerId,
        String ownerName,
        String professionalId,
        String professionalName,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String reason,
        AppointmentStatus status,
        String statusLabel,
        String notes,
        String cancellationReason,
        Instant createdAt) {

    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(appointment.getId(), appointment.getClinicId(), appointment.getClinicName(),
                appointment.getPetId(), appointment.getPetName(), appointment.getPetSpecies(), appointment.getOwnerId(),
                appointment.getOwnerName(), appointment.getProfessionalId(), appointment.getProfessionalName(),
                appointment.getDate(), appointment.getStartTime(), appointment.getEndTime(), appointment.getReason(),
                appointment.getStatus(), appointment.getStatus().getLabel(), appointment.getNotes(),
                appointment.getCancellationReason(), appointment.getCreatedAt());
    }
}
