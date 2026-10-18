package com.vetcare.facturacion.web.dto;

import java.math.BigDecimal;

import com.vetcare.facturacion.domain.PaymentMethod;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PaymentRequest(
        @NotNull(message = "El valor del pago es obligatorio") @DecimalMin(value = "0.01", message = "El pago debe ser mayor que cero") BigDecimal amount,
        @NotNull(message = "El medio de pago es obligatorio") PaymentMethod method,
        @Size(max = 80) String reference) {
}
