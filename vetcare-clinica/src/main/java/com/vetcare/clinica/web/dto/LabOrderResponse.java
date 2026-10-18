package com.vetcare.clinica.web.dto;

import java.time.Instant;

import com.vetcare.clinica.domain.LabOrder;
import com.vetcare.clinica.domain.LabStatus;

public record LabOrderResponse(String id, String recordId, String appointmentId, String petId, String petName,
        String examName, String notes, LabStatus status, String statusLabel, Instant orderedAt, String orderedBy,
        String resultValues, String resultObservations, Instant resultAt, String resultBy) {

    public static LabOrderResponse from(LabOrder order) {
        return new LabOrderResponse(order.getId(), order.getRecordId(), order.getAppointmentId(), order.getPetId(),
                order.getPetName(), order.getExamName(), order.getNotes(), order.getStatus(),
                order.getStatus().getLabel(), order.getOrderedAt(), order.getOrderedBy(), order.getResultValues(),
                order.getResultObservations(), order.getResultAt(), order.getResultBy());
    }
}
