package com.vetcare.pacientes.web.dto;

import java.time.LocalDate;
import java.util.Map;

/** Cifras del dia que alimentan el tablero administrativo. */
public record AppointmentSummary(LocalDate date, long total, long attended, long pending, long cancelled,
        Map<String, Long> byStatus) {
}
