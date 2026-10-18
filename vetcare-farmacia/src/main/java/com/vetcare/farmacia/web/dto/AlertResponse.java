package com.vetcare.farmacia.web.dto;

import java.time.LocalDate;

/** Alerta de inventario: stock bajo el minimo o lote proximo a vencer. */
public record AlertResponse(String type, String medicationId, String medicationName, String message, int quantity,
        LocalDate expiresAt) {
}
