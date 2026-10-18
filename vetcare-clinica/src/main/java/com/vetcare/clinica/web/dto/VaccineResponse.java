package com.vetcare.clinica.web.dto;

import com.vetcare.clinica.domain.Species;
import com.vetcare.clinica.domain.Vaccine;

public record VaccineResponse(String id, String name, Species species, int nextDoseDays, boolean active) {

    public static VaccineResponse from(Vaccine vaccine) {
        return new VaccineResponse(vaccine.getId(), vaccine.getName(), vaccine.getSpecies(),
                vaccine.getNextDoseDays(), vaccine.isActive());
    }
}
