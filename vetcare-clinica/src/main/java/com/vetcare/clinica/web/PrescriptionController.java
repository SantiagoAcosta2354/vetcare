package com.vetcare.clinica.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vetcare.clinica.domain.PrescriptionStatus;
import com.vetcare.clinica.service.PrescriptionService;
import com.vetcare.clinica.web.dto.PrescriptionResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/prescriptions")
@Tag(name = "Formulas", description = "Emision y estado de despacho")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @GetMapping
    @Operation(summary = "Formulas por historia, por paciente o por estado")
    public List<PrescriptionResponse> list(@RequestParam(required = false) String recordId,
            @RequestParam(required = false) String petId,
            @RequestParam(required = false) PrescriptionStatus status) {
        if (recordId != null && !recordId.isBlank()) {
            return prescriptionService.findByRecord(recordId);
        }
        if (petId != null && !petId.isBlank()) {
            return prescriptionService.findByPet(petId);
        }
        if (status != null) {
            return prescriptionService.findByStatus(status);
        }
        return prescriptionService.findByStatus(PrescriptionStatus.EMITIDA);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta una formula")
    public PrescriptionResponse findById(@PathVariable String id) {
        return prescriptionService.findById(id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Farmacia marca la formula como despachada o pendiente por stock")
    public PrescriptionResponse updateStatus(@PathVariable String id, @RequestParam PrescriptionStatus status) {
        return prescriptionService.updateDispensingStatus(id, status);
    }
}
