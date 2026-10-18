package com.vetcare.pacientes.domain;

/** Especies atendidas. El alcance del proyecto excluye otras especies. */
public enum Species {
    CANINO("Canino"),
    FELINO("Felino");

    private final String label;

    Species(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
