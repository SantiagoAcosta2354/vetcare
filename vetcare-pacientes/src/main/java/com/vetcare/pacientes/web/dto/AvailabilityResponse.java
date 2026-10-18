package com.vetcare.pacientes.web.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Franjas del dia para un profesional: la agenda solo ofrece lo que esta libre. */
public record AvailabilityResponse(LocalDate date, String professionalId, List<Slot> slots) {

    public record Slot(LocalTime start, LocalTime end, boolean available) {
    }
}
