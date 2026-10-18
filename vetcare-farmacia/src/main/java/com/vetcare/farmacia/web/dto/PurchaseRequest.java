package com.vetcare.farmacia.web.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Entrada de mercancia: sin lote y sin vencimiento no se recibe nada. */
public record PurchaseRequest(
        @NotBlank(message = "El medicamento es obligatorio") String medicationId,
        @NotBlank(message = "El numero de lote es obligatorio") @Size(max = 40) String batchNumber,
        @NotNull(message = "La fecha de vencimiento es obligatoria") @Future(message = "No se recibe un lote ya vencido") LocalDate expiresAt,
        @Min(value = 1, message = "La cantidad debe ser mayor que cero") int quantity,
        @Size(max = 120) String supplier) {
}
