package com.vetcare.farmacia.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vetcare.farmacia.service.MedicationService;
import com.vetcare.farmacia.web.dto.BatchResponse;
import com.vetcare.farmacia.web.dto.MedicationRequest;
import com.vetcare.farmacia.web.dto.MedicationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/medications")
@Tag(name = "Catalogo de farmacia", description = "Medicamentos, existencia y lotes")
public class MedicationController {

    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping
    @Operation(summary = "Lista o busca medicamentos con su existencia real")
    public List<MedicationResponse> list(@RequestParam(required = false) String q) {
        return medicationService.findAll(q);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta un medicamento y su existencia")
    public MedicationResponse findById(@PathVariable String id) {
        return medicationService.findById(id);
    }

    @GetMapping("/{id}/batches")
    @Operation(summary = "Lotes de un medicamento con su vencimiento")
    public List<BatchResponse> batches(@PathVariable String id) {
        return medicationService.batchesOf(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un medicamento en el catalogo")
    public MedicationResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody MedicationRequest request) {
        return medicationService.create(request, jwt.getSubject());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza los datos del catalogo")
    public MedicationResponse update(@PathVariable String id, @Valid @RequestBody MedicationRequest request) {
        return medicationService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activa o retira un medicamento del catalogo")
    public MedicationResponse changeStatus(@PathVariable String id, @RequestParam boolean active) {
        return medicationService.changeStatus(id, active);
    }
}
