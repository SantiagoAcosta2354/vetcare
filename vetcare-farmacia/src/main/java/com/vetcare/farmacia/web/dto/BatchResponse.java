package com.vetcare.farmacia.web.dto;

import java.time.LocalDate;

import com.vetcare.farmacia.domain.Batch;

public record BatchResponse(String id, String medicationId, String medicationName, String batchNumber,
        LocalDate expiresAt, int quantity, String supplier, boolean expired, boolean expiringSoon) {

    public static BatchResponse from(Batch batch, LocalDate today, int alertDays) {
        boolean expired = batch.getExpiresAt().isBefore(today);
        boolean soon = !expired && !batch.getExpiresAt().isAfter(today.plusDays(alertDays));
        return new BatchResponse(batch.getId(), batch.getMedicationId(), batch.getMedicationName(),
                batch.getBatchNumber(), batch.getExpiresAt(), batch.getQuantity(), batch.getSupplier(), expired, soon);
    }
}
