package com.vetcare.clinica.web.dto;

import java.time.Instant;

import com.vetcare.clinica.domain.MedicalRecord;
import com.vetcare.clinica.domain.RecordStatus;
import com.vetcare.clinica.domain.Species;

public record RecordResponse(String id, String appointmentId, String petId, String petName, Species petSpecies,
        String ownerId, String ownerName, String professionalId, String professionalName, String reason,
        RecordStatus status, String statusLabel, Double weightKg, Double temperatureC, Integer heartRate,
        Integer respiratoryRate, String diagnosis, String treatmentDescription, Integer treatmentDurationDays,
        String treatmentIndications, String notes, String closureNote, Instant openedAt, Instant closedAt,
        String closedBy) {

    public static RecordResponse from(MedicalRecord record) {
        return new RecordResponse(record.getId(), record.getAppointmentId(), record.getPetId(), record.getPetName(),
                record.getPetSpecies(), record.getOwnerId(), record.getOwnerName(), record.getProfessionalId(),
                record.getProfessionalName(), record.getReason(), record.getStatus(), record.getStatus().getLabel(),
                record.getWeightKg(), record.getTemperatureC(), record.getHeartRate(), record.getRespiratoryRate(),
                record.getDiagnosis(), record.getTreatmentDescription(), record.getTreatmentDurationDays(),
                record.getTreatmentIndications(), record.getNotes(), record.getClosureNote(), record.getOpenedAt(),
                record.getClosedAt(), record.getClosedBy());
    }
}
