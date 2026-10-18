package com.vetcare.facturacion.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Motivo obligatorio para anular una factura o un pago. */
public record ReasonRequest(@NotBlank(message = "El motivo es obligatorio") @Size(max = 300) String reason) {
}
