package com.vetcare.farmacia.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Despacho de una formula. Es un movimiento de inventario, no un documento
 * clinico: guarda que se entrego, de que lote y quien lo hizo.
 *
 * Si no hay existencia suficiente, el documento queda en PENDIENTE_POR_STOCK y
 * no descuenta nada. Un despacho a medias seria peor que ninguno, porque la
 * factura cobraria producto que el paciente no recibio.
 */
@Document(collection = "dispensings")
public class Dispensing {

    @Id
    private String id;

    private String prescriptionId;
    private String recordId;
    private String petId;
    private String petName;
    private String ownerId;

    private List<DispensingItem> items = new ArrayList<>();

    private DispensingStatus status = DispensingStatus.PENDIENTE_POR_STOCK;
    private String notes;
    private String dispensedBy;
    private Instant dispensedAt;

    @CreatedDate
    private Instant createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(String prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getPetId() {
        return petId;
    }

    public void setPetId(String petId) {
        this.petId = petId;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public List<DispensingItem> getItems() {
        return items;
    }

    public void setItems(List<DispensingItem> items) {
        this.items = items;
    }

    public DispensingStatus getStatus() {
        return status;
    }

    public void setStatus(DispensingStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDispensedBy() {
        return dispensedBy;
    }

    public void setDispensedBy(String dispensedBy) {
        this.dispensedBy = dispensedBy;
    }

    public Instant getDispensedAt() {
        return dispensedAt;
    }

    public void setDispensedAt(Instant dispensedAt) {
        this.dispensedAt = dispensedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
