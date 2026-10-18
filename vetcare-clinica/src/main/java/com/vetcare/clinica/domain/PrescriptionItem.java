package com.vetcare.clinica.domain;

public class PrescriptionItem {

    private String medicationId;
    private String medicationName;
    private String dose;
    private String frequency;
    private Integer durationDays;
    private int quantity;

    public PrescriptionItem() {
    }

    public PrescriptionItem(String medicationId, String medicationName, String dose, String frequency,
            Integer durationDays, int quantity) {
        this.medicationId = medicationId;
        this.medicationName = medicationName;
        this.dose = dose;
        this.frequency = frequency;
        this.durationDays = durationDays;
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

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
