package com.vetcare.pacientes.domain;

public enum DocumentType {
    CC("Cedula de ciudadania"),
    CE("Cedula de extranjeria"),
    NIT("NIT"),
    PASAPORTE("Pasaporte");

    private final String label;

    DocumentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
