package com.vetcare.clinica.domain;

/** Estado de la formula. Solo farmacia la pasa a DESPACHADA o PENDIENTE_POR_STOCK. */
public enum PrescriptionStatus {
    EMITIDA("Emitida"),
    DESPACHADA("Despachada"),
    PENDIENTE_POR_STOCK("Pendiente por stock"),
    ANULADA("Anulada");

    private final String label;

    PrescriptionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean blocksClosure() {
        return this == EMITIDA || this == PENDIENTE_POR_STOCK;
    }
}
