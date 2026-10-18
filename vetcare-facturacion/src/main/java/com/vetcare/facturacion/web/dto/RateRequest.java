package com.vetcare.facturacion.web.dto;

import java.math.BigDecimal;

import com.vetcare.facturacion.domain.ItemType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RateRequest(
        @NotBlank(message = "El codigo de la tarifa es obligatorio") @Size(max = 40) String code,
        @NotBlank(message = "El nombre de la tarifa es obligatorio") @Size(max = 120) String name,
        @NotNull(message = "El tipo es obligatorio") ItemType type,
        @NotNull(message = "El precio es obligatorio") @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo") BigDecimal price,
        @DecimalMin(value = "0.0", inclusive = true, message = "El IVA no puede ser negativo") BigDecimal taxRate) {
}
