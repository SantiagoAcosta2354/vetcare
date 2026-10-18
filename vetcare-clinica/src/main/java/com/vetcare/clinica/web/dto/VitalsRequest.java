package com.vetcare.clinica.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Signos vitales con unidad: peso en kg y temperatura en grados centigrados. */
public record VitalsRequest(
        @DecimalMin(value = "0.1", message = "El peso debe ser mayor que cero") @DecimalMax(value = "150", message = "El peso no es plausible") Double weightKg,
        @DecimalMin(value = "30.0", message = "La temperatura no es plausible") @DecimalMax(value = "45.0", message = "La temperatura no es plausible") Double temperatureC,
        @Positive(message = "La frecuencia cardiaca debe ser positiva") Integer heartRate,
        @Positive(message = "La frecuencia respiratoria debe ser positiva") Integer respiratoryRate,
        @Size(max = 500) String notes) {
}
