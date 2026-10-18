package com.vetcare.pacientes.web.dto;

import java.time.LocalTime;

import com.vetcare.pacientes.domain.Clinic;

public record ClinicResponse(String id, String name, String address, String phone, LocalTime openingTime,
        LocalTime closingTime, int slotMinutes, boolean active) {

    public static ClinicResponse from(Clinic clinic) {
        return new ClinicResponse(clinic.getId(), clinic.getName(), clinic.getAddress(), clinic.getPhone(),
                clinic.getOpeningTime(), clinic.getClosingTime(), clinic.getSlotMinutes(), clinic.isActive());
    }
}
