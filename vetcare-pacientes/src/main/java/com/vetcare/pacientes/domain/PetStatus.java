package com.vetcare.pacientes.domain;

/**
 * Estado del paciente. SUSPENDIDO es la baja solicitada por el dueno: conserva
 * la historia y las facturas, pero no admite citas nuevas.
 */
public enum PetStatus {
    ACTIVO,
    SUSPENDIDO
}
