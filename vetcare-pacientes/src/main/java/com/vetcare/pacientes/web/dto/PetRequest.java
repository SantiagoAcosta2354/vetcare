package com.vetcare.pacientes.web.dto;

import java.time.LocalDate;

import com.vetcare.pacientes.domain.Species;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PetRequest(
        @NotBlank(message = "El dueno es obligatorio") String ownerId,
        @NotBlank(message = "El nombre de la mascota es obligatorio") @Size(max = 80) String name,
        @NotNull(message = "La especie es obligatoria") Species species,
        @Size(max = 80) String breed,
        String sex,
        @PastOrPresent(message = "La fecha de nacimiento no puede ser futura") LocalDate birthDate,
        @Positive(message = "El peso debe ser mayor que cero") Double weightKg,
        @Size(max = 40) String microchip,
        @Size(max = 500) String notes) {
}
