package com.vetcare.clinica.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** El resultado guarda los valores y la fecha en que se registro. */
public record LabResultRequest(
        @NotBlank(message = "El resultado es obligatorio") @Size(max = 2000) String values,
        @Size(max = 500) String observations) {
}
