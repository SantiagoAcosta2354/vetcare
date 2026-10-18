package com.vetcare.pacientes.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Estados de la cita y transiciones permitidas.
 *
 * La regla se declara aqui, en el dominio, y no en el controlador: cualquier
 * camino que intente un salto no permitido (por ejemplo ATENDIDA -> AGENDADA)
 * falla igual.
 */
public enum AppointmentStatus {

    AGENDADA("Agendada"),
    CONFIRMADA("Confirmada"),
    EN_ATENCION("En atencion"),
    ATENDIDA("Atendida"),
    CANCELADA("Cancelada"),
    NO_ASISTIO("No asistio");

    private final String label;

    AppointmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isFinal() {
        return this == ATENDIDA || this == CANCELADA || this == NO_ASISTIO;
    }

    /** Estados que ocupan la franja del profesional. */
    public boolean occupiesSlot() {
        return this == AGENDADA || this == CONFIRMADA || this == EN_ATENCION;
    }

    public Set<AppointmentStatus> allowedNext() {
        return switch (this) {
            case AGENDADA -> EnumSet.of(CONFIRMADA, EN_ATENCION, CANCELADA, NO_ASISTIO);
            case CONFIRMADA -> EnumSet.of(EN_ATENCION, CANCELADA, NO_ASISTIO);
            case EN_ATENCION -> EnumSet.of(ATENDIDA, CANCELADA);
            case ATENDIDA, CANCELADA, NO_ASISTIO -> EnumSet.noneOf(AppointmentStatus.class);
        };
    }
}
