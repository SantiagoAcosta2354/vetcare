package com.vetcare.clinica.web.dto;

import jakarta.validation.constraints.Size;

public record CloseRecordRequest(@Size(max = 500) String note) {
}
