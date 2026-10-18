package com.vetcare.clinica.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DiagnosisRequest(
        @NotBlank(message = "El diagnostico es obligatorio") @Size(max = 1000) String diagnosis,
        @NotBlank(message = "El tratamiento es obligatorio") @Size(max = 1000) String treatmentDescription,
        @Min(value = 1, message = "La duracion del tratamiento debe ser al menos de un dia") Integer treatmentDurationDays,
        @Size(max = 1000) String treatmentIndications,
        @Size(max = 500) String notes) {
}
