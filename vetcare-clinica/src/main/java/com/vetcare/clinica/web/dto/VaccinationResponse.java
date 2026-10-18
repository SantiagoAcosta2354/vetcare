package com.vetcare.clinica.web.dto;

import java.time.LocalDate;

import com.vetcare.clinica.domain.Vaccination;

public record VaccinationResponse(String id, String recordId, String petId, String petName, String vaccineId,
        String vaccineName, String batch, LocalDate appliedAt, LocalDate nextDoseAt, String appliedBy) {

    public static VaccinationResponse from(Vaccination vaccination) {
        return new VaccinationResponse(vaccination.getId(), vaccination.getRecordId(), vaccination.getPetId(),
                vaccination.getPetName(), vaccination.getVaccineId(), vaccination.getVaccineName(),
                vaccination.getBatch(), vaccination.getAppliedAt(), vaccination.getNextDoseAt(),
                vaccination.getAppliedBy());
    }
}
