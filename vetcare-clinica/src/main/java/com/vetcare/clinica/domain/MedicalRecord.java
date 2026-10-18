package com.vetcare.clinica.domain;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Historia clinica de una atencion. Se abre una sola vez por cita (indice unico
 * sobre appointmentId) y se cierra cuando el veterinario termina.
 */
@Document(collection = "medicalRecords")
public class MedicalRecord {

    @Id
    private String id;

    private String appointmentId;
    private String petId;
    private String petName;
    private Species petSpecies;
    private String ownerId;
    private String ownerName;

    private String professionalId;
    private String professionalName;
    private String reason;

    private RecordStatus status = RecordStatus.ABIERTA;

    // Signos vitales
    private Double weightKg;
    private Double temperatureC;
    private Integer heartRate;
    private Integer respiratoryRate;

    // Diagnostico y tratamiento
    private String diagnosis;
    private String treatmentDescription;
    private Integer treatmentDurationDays;
    private String treatmentIndications;

    private String notes;
    private String closureNote;

    private Instant openedAt;
    private Instant closedAt;
    private String closedBy;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Species getPetSpecies() {
        return petSpecies;
    }

    public void setPetSpecies(Species petSpecies) {
        this.petSpecies = petSpecies;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getProfessionalId() {
        return professionalId;
    }

    public void setProfessionalId(String professionalId) {
        this.professionalId = professionalId;
    }

    public String getProfessionalName() {
        return professionalName;
    }

    public void setProfessionalName(String professionalName) {
        this.professionalName = professionalName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public RecordStatus getStatus() {
        return status;
    }

    public void setStatus(RecordStatus status) {
        this.status = status;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public Double getTemperatureC() {
        return temperatureC;
    }

    public void setTemperatureC(Double temperatureC) {
        this.temperatureC = temperatureC;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public Integer getRespiratoryRate() {
        return respiratoryRate;
    }

    public void setRespiratoryRate(Integer respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatmentDescription() {
        return treatmentDescription;
    }

    public void setTreatmentDescription(String treatmentDescription) {
        this.treatmentDescription = treatmentDescription;
    }

    public Integer getTreatmentDurationDays() {
        return treatmentDurationDays;
    }

    public void setTreatmentDurationDays(Integer treatmentDurationDays) {
        this.treatmentDurationDays = treatmentDurationDays;
    }

    public String getTreatmentIndications() {
        return treatmentIndications;
    }

    public void setTreatmentIndications(String treatmentIndications) {
        this.treatmentIndications = treatmentIndications;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getClosureNote() {
        return closureNote;
    }

    public void setClosureNote(String closureNote) {
        this.closureNote = closureNote;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(Instant openedAt) {
        this.openedAt = openedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public String getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
