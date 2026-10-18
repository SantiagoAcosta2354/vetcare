package com.vetcare.facturacion.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.vetcare.facturacion.domain.ItemType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InvoiceRequest(
        @NotBlank(message = "El dueno es obligatorio") String ownerId,
        String ownerName,
        @NotBlank(message = "El paciente es obligatorio") String petId,
        String petName,
        String appointmentId,
        @NotNull(message = "La fecha de la atencion es obligatoria") LocalDate consultationDate,
        @NotEmpty(message = "La factura debe tener al menos un item") @Valid List<Item> items) {

    public record Item(
            @NotBlank(message = "El codigo es obligatorio") @Size(max = 40) String code,
            @NotBlank(message = "La descripcion es obligatoria") @Size(max = 200) String description,
            @NotNull(message = "El tipo es obligatorio") ItemType type,
            String referenceId,
            @Min(value = 1, message = "La cantidad debe ser mayor que cero") int quantity,
            @NotNull(message = "El precio unitario es obligatorio") @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo") BigDecimal unitPrice) {
    }
}
