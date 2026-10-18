package com.vetcare.clinica.domain;

/** La orden nace pendiente y solo el laboratorio la cierra con el resultado. */
public enum LabStatus {
    PENDIENTE("Pendiente"),
    ENTREGADO("Entregado");

    private final String label;

    LabStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
