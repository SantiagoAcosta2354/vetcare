package com.vetcare.farmacia.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vetcare.farmacia.service.MedicationService;
import com.vetcare.farmacia.web.dto.AlertResponse;
import com.vetcare.farmacia.web.dto.BatchResponse;
import com.vetcare.farmacia.web.dto.PurchaseRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Inventario", description = "Compras, alertas de stock y vencimientos")
public class InventoryController {

    private final MedicationService medicationService;

    public InventoryController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @PostMapping("/purchases")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Recibe mercancia: exige lote y fecha de vencimiento")
    public BatchResponse receive(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PurchaseRequest request) {
        return medicationService.receive(request, jwt.getSubject());
    }

    @GetMapping("/alerts")
    @Operation(summary = "Alertas de stock bajo el minimo y lotes por vencer")
    public List<AlertResponse> alerts() {
        return medicationService.alerts();
    }
}
