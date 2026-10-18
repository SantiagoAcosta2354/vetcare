package com.vetcare.farmacia.web.dto;

import java.time.Instant;
import java.util.List;

import com.vetcare.farmacia.domain.Dispensing;
import com.vetcare.farmacia.domain.DispensingItem;
import com.vetcare.farmacia.domain.DispensingStatus;

public record DispensingResponse(String id, String prescriptionId, String recordId, String petId, String petName,
        String ownerId, List<DispensingItem> items, DispensingStatus status, String statusLabel, String dispensedBy,
        Instant dispensedAt, String notes) {

    public static DispensingResponse from(Dispensing dispensing) {
        return new DispensingResponse(dispensing.getId(), dispensing.getPrescriptionId(), dispensing.getRecordId(),
                dispensing.getPetId(), dispensing.getPetName(), dispensing.getOwnerId(), dispensing.getItems(),
                dispensing.getStatus(), dispensing.getStatus().getLabel(), dispensing.getDispensedBy(),
                dispensing.getDispensedAt(), dispensing.getNotes());
    }
}
