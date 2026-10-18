package com.vetcare.clinica.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Orden de examen. Nace PENDIENTE y solo el laboratorio la cierra al registrar
 * el resultado; sin resultado no se puede cobrar como entregado.
 */
@Document(collection = "labOrders")
public class LabOrder {

    @Id
    private String id;

    private String recordId;
    private String appointmentId;
    private String petId;
    private String petName;
    private String ownerId;

    private String examName;
    private String notes;
    private LabStatus status = LabStatus.PENDIENTE;

    private Instant orderedAt;
    private String orderedBy;

    private String resultValues;
    private String resultObservations;
    private Instant resultAt;
    private String resultBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
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

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LabStatus getStatus() {
        return status;
    }

    public void setStatus(LabStatus status) {
        this.status = status;
    }

    public Instant getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(Instant orderedAt) {
        this.orderedAt = orderedAt;
    }

    public String getOrderedBy() {
        return orderedBy;
    }

    public void setOrderedBy(String orderedBy) {
        this.orderedBy = orderedBy;
    }

    public String getResultValues() {
        return resultValues;
    }

    public void setResultValues(String resultValues) {
        this.resultValues = resultValues;
    }

    public String getResultObservations() {
        return resultObservations;
    }

    public void setResultObservations(String resultObservations) {
        this.resultObservations = resultObservations;
    }

    public Instant getResultAt() {
        return resultAt;
    }

    public void setResultAt(Instant resultAt) {
        this.resultAt = resultAt;
    }

    public String getResultBy() {
        return resultBy;
    }

    public void setResultBy(String resultBy) {
        this.resultBy = resultBy;
    }
}
