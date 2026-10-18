package com.vetcare.clinica.web;

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

import com.vetcare.clinica.service.LabOrderService;
import com.vetcare.clinica.service.MedicalRecordService;
import com.vetcare.clinica.service.PrescriptionService;
import com.vetcare.clinica.service.VaccinationService;
import com.vetcare.clinica.web.dto.CloseRecordRequest;
import com.vetcare.clinica.web.dto.DiagnosisRequest;
import com.vetcare.clinica.web.dto.LabOrderRequest;
import com.vetcare.clinica.web.dto.LabOrderResponse;
import com.vetcare.clinica.web.dto.OpenRecordRequest;
import com.vetcare.clinica.web.dto.PrescriptionRequest;
import com.vetcare.clinica.web.dto.PrescriptionResponse;
import com.vetcare.clinica.web.dto.RecordResponse;
import com.vetcare.clinica.web.dto.VaccinationRequest;
import com.vetcare.clinica.web.dto.VaccinationResponse;
import com.vetcare.clinica.web.dto.VitalsRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/records")
@Tag(name = "Historia clinica", description = "Apertura, signos vitales, diagnostico, formula, examenes y vacunas")
public class RecordController {

    private final MedicalRecordService recordService;
    private final PrescriptionService prescriptionService;
    private final LabOrderService labOrderService;
    private final VaccinationService vaccinationService;

    public RecordController(MedicalRecordService recordService, PrescriptionService prescriptionService,
            LabOrderService labOrderService, VaccinationService vaccinationService) {
        this.recordService = recordService;
        this.prescriptionService = prescriptionService;
        this.labOrderService = labOrderService;
        this.vaccinationService = vaccinationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Abre la historia clinica de una cita (una sola por cita)")
    public RecordResponse open(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody OpenRecordRequest request) {
        return recordService.open(request, jwt.getSubject());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta una historia clinica")
    public RecordResponse findById(@PathVariable String id) {
        return recordService.findById(id);
    }

    @GetMapping("/by-appointment/{appointmentId}")
    @Operation(summary = "Historia de una cita")
    public RecordResponse findByAppointment(@PathVariable String appointmentId) {
        return recordService.findByAppointment(appointmentId);
    }

    @GetMapping
    @Operation(summary = "Historias de un paciente")
    public List<RecordResponse> findByPet(@RequestParam String petId) {
        return recordService.findByPet(petId);
    }

    @PatchMapping("/{id}/vitals")
    @Operation(summary = "Registra peso, temperatura y frecuencias")
    public RecordResponse vitals(@PathVariable String id, @Valid @RequestBody VitalsRequest request) {
        return recordService.updateVitals(id, request);
    }

    @PatchMapping("/{id}/diagnosis")
    @Operation(summary = "Registra diagnostico y tratamiento")
    public RecordResponse diagnosis(@PathVariable String id, @Valid @RequestBody DiagnosisRequest request) {
        return recordService.updateDiagnosis(id, request);
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Cierra la historia; deja de admitir cambios")
    public RecordResponse close(@AuthenticationPrincipal Jwt jwt, @PathVariable String id,
            @RequestBody(required = false) CloseRecordRequest request) {
        return recordService.close(id, request == null ? null : request.note(), jwt.getSubject());
    }

    @PostMapping("/{id}/prescriptions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Emite una formula dentro de la consulta")
    public PrescriptionResponse prescribe(@AuthenticationPrincipal Jwt jwt, @PathVariable String id,
            @Valid @RequestBody PrescriptionRequest request) {
        return prescriptionService.issue(id, request, jwt.getSubject());
    }

    @PostMapping("/{id}/lab-orders")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ordena un examen de laboratorio")
    public LabOrderResponse orderLab(@AuthenticationPrincipal Jwt jwt, @PathVariable String id,
            @Valid @RequestBody LabOrderRequest request) {
        return labOrderService.order(id, request, jwt.getSubject());
    }

    @PostMapping("/{id}/vaccinations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Aplica una vacuna y calcula la proxima dosis")
    public VaccinationResponse vaccinate(@AuthenticationPrincipal Jwt jwt, @PathVariable String id,
            @Valid @RequestBody VaccinationRequest request) {
        return vaccinationService.apply(id, request, jwt.getSubject());
    }
}
