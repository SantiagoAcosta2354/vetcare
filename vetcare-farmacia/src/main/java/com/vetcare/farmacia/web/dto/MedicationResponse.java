package com.vetcare.farmacia.web.dto;

import com.vetcare.farmacia.domain.Medication;

/** El stock disponible se calcula de los lotes vigentes, no se guarda. */
public record MedicationResponse(String id, String name, String activeIngredient, String presentation, String unit,
        int stockMin, int available, boolean lowStock, boolean active) {

    public static MedicationResponse from(Medication medication, int available) {
        return new MedicationResponse(medication.getId(), medication.getName(), medication.getActiveIngredient(),
                medication.getPresentation(), medication.getUnit(), medication.getStockMin(), available,
                available < medication.getStockMin(), medication.isActive());
    }
}
