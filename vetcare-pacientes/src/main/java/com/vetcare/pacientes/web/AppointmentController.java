package com.vetcare.pacientes.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vetcare.pacientes.service.AppointmentService;
import com.vetcare.pacientes.web.dto.AppointmentRequest;
import com.vetcare.pacientes.web.dto.AppointmentResponse;
import com.vetcare.pacientes.web.dto.AppointmentSummary;
import com.vetcare.pacientes.web.dto.AvailabilityResponse;
import com.vetcare.pacientes.web.dto.StatusChangeRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/appointments")
@Tag(name = "Agenda", description = "Citas, disponibilidad y transiciones de estado")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Agenda una cita si la franja del profesional esta libre")
    public AppointmentResponse schedule(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AppointmentRequest request) {
        return appointmentService.schedule(request, jwt.getSubject());
    }

    @GetMapping
    @Operation(summary = "Agenda del dia, opcionalmente filtrada por sede")
    public List<AppointmentResponse> byDate(@RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String clinicId) {
        return appointmentService.findByDate(date == null ? LocalDate.now() : date, clinicId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta una cita")
    public AppointmentResponse findById(@PathVariable String id) {
        return appointmentService.findById(id);
    }

    @GetMapping("/professional")
    @Operation(summary = "Citas de un profesional en una fecha")
    public List<AppointmentResponse> byProfessional(@RequestParam LocalDate date,
            @RequestParam String professionalId) {
        return appointmentService.findByProfessional(date, professionalId);
    }

    @GetMapping("/pet/{petId}")
    @Operation(summary = "Historial de citas de un paciente")
    public List<AppointmentResponse> byPet(@PathVariable String petId) {
        return appointmentService.findByPet(petId);
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Citas de un dueno")
    public List<AppointmentResponse> byOwner(@PathVariable String ownerId) {
        return appointmentService.findByOwner(ownerId);
    }

    @GetMapping("/range")
    @Operation(summary = "Citas entre dos fechas")
    public List<AppointmentResponse> between(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        return appointmentService.findBetween(from, to);
    }

    @GetMapping("/availability")
    @Operation(summary = "Franjas del dia con su disponibilidad real")
    public AvailabilityResponse availability(@RequestParam String professionalId, @RequestParam String clinicId,
            @RequestParam LocalDate date) {
        return appointmentService.availability(professionalId, clinicId, date);
    }

    @GetMapping("/summary")
    @Operation(summary = "Cifras del dia para el tablero")
    public AppointmentSummary summary(@RequestParam(required = false) LocalDate date) {
        return appointmentService.summary(date == null ? LocalDate.now() : date);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Confirma, atiende, cancela o marca inasistencia")
    public AppointmentResponse changeStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable String id,
            @Valid @RequestBody StatusChangeRequest request) {
        return appointmentService.changeStatus(id, request.status(), request.reason(), jwt.getSubject());
    }
}
