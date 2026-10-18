package com.vetcare.farmacia.domain;

/** Estado del despacho de una formula. */
public enum DispensingStatus {
    DESPACHADA("Despachada"),
    PENDIENTE_POR_STOCK("Pendiente por stock"),
    ANULADA("Anulada");

    private final String label;

    DispensingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
