package com.vetcare.clinica.web;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vetcare.clinica.domain.LabStatus;
import com.vetcare.clinica.service.LabOrderService;
import com.vetcare.clinica.web.dto.LabOrderResponse;
import com.vetcare.clinica.web.dto.LabResultRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/lab-orders")
@Tag(name = "Laboratorio", description = "Ordenes pendientes y registro de resultados")
public class LabOrderController {

    private final LabOrderService labOrderService;

    public LabOrderController(LabOrderService labOrderService) {
        this.labOrderService = labOrderService;
    }

    @GetMapping
    @Operation(summary = "Ordenes por estado (por defecto, las pendientes)")
    public List<LabOrderResponse> list(@RequestParam(required = false) LabStatus status,
            @RequestParam(required = false) String petId) {
        if (petId != null && !petId.isBlank()) {
            return labOrderService.findByPet(petId);
        }
        return labOrderService.findByStatus(status == null ? LabStatus.PENDIENTE : status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta una orden")
    public LabOrderResponse findById(@PathVariable String id) {
        return labOrderService.findById(id);
    }

    @PostMapping("/{id}/result")
    @Operation(summary = "El laboratorio registra el resultado y cierra la orden")
    public LabOrderResponse registerResult(@AuthenticationPrincipal Jwt jwt, @PathVariable String id,
            @Valid @RequestBody LabResultRequest request) {
        return labOrderService.registerResult(id, request, jwt.getSubject());
    }
}
