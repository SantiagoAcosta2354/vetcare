package com.vetcare.farmacia.domain;

import java.util.ArrayList;
import java.util.List;

public class DispensingItem {

    private String medicationId;
    private String medicationName;
    private int quantity;
    private List<BatchAllocation> allocations = new ArrayList<>();

    public DispensingItem() {
    }

    public DispensingItem(String medicationId, String medicationName, int quantity) {
        this.medicationId = medicationId;
        this.medicationName = medicationName;
        this.quantity = quantity;
    }

    public String getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(String medicationId) {
        this.medicationId = medicationId;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public List<BatchAllocation> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<BatchAllocation> allocations) {
        this.allocations = allocations;
    }
}
