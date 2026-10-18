package com.vetcare.farmacia.domain;

import java.time.LocalDate;

/**
 * Traza de que lote salio cada unidad despachada. Sin esto no se puede responder
 * un reclamo del dueno ni un retiro de lote del proveedor.
 */
public class BatchAllocation {

    private String batchId;
    private String batchNumber;
    private LocalDate expiresAt;
    private int quantity;

    public BatchAllocation() {
    }

    public BatchAllocation(String batchId, String batchNumber, LocalDate expiresAt, int quantity) {
        this.batchId = batchId;
        this.batchNumber = batchNumber;
        this.expiresAt = expiresAt;
        this.quantity = quantity;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public LocalDate getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDate expiresAt) {
        this.expiresAt = expiresAt;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
