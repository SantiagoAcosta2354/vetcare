package com.vetcare.clinica.web.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record VaccinationRequest(
        @NotBlank(message = "La vacuna es obligatoria") String vaccineId,
        @Size(max = 40) String batch,
        @NotNull(message = "La fecha de aplicacion es obligatoria") @PastOrPresent(message = "La fecha de aplicacion no puede ser futura") LocalDate appliedAt) {
}
