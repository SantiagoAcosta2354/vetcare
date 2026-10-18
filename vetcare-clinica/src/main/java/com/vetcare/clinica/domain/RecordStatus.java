package com.vetcare.clinica.domain;

/**
 * Una historia abierta admite cambios; una cerrada no. El cierre es lo que
 * convierte la atencion en documento: si se pudiera editar despues, la
 * trazabilidad clinica no valdria nada.
 */
public enum RecordStatus {
    ABIERTA("Abierta"),
    CERRADA("Cerrada");

    private final String label;

    RecordStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
