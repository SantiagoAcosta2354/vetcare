package com.vetcare.clinica.web.dto;

import java.time.Instant;
import java.util.List;

import com.vetcare.clinica.domain.Prescription;
import com.vetcare.clinica.domain.PrescriptionItem;
import com.vetcare.clinica.domain.PrescriptionStatus;

public record PrescriptionResponse(String id, String recordId, String appointmentId, String petId, String petName,
        String ownerId, List<PrescriptionItem> items, PrescriptionStatus status, String statusLabel,
        String observations, Instant issuedAt, String issuedBy) {

    public static PrescriptionResponse from(Prescription prescription) {
        return new PrescriptionResponse(prescription.getId(), prescription.getRecordId(),
                prescription.getAppointmentId(), prescription.getPetId(), prescription.getPetName(),
                prescription.getOwnerId(), prescription.getItems(), prescription.getStatus(),
                prescription.getStatus().getLabel(), prescription.getObservations(), prescription.getIssuedAt(),
                prescription.getIssuedBy());
    }
}
