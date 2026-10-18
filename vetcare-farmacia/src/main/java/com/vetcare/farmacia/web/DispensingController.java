package com.vetcare.farmacia.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vetcare.farmacia.domain.DispensingStatus;
import com.vetcare.farmacia.service.DispensingService;
import com.vetcare.farmacia.web.dto.DispensingRequest;
import com.vetcare.farmacia.web.dto.DispensingResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/dispensings")
@Tag(name = "Despachos", description = "Entrega de formulas con descuento de existencias")
public class DispensingController {

    private final DispensingService dispensingService;

    public DispensingController(DispensingService dispensingService) {
        this.dispensingService = dispensingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Despacha una formula descontando lotes vigentes")
    public DispensingResponse dispense(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody DispensingRequest request) {
        return dispensingService.dispense(request, jwt.getSubject());
    }

    @GetMapping
    @Operation(summary = "Despachos por estado o por paciente")
    public List<DispensingResponse> list(@RequestParam(required = false) DispensingStatus status,
            @RequestParam(required = false) String petId) {
        if (petId != null && !petId.isBlank()) {
            return dispensingService.findByPet(petId);
        }
        if (status != null) {
            return dispensingService.findByStatus(status);
        }
        return dispensingService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta un despacho con los lotes entregados")
    public DispensingResponse findById(@PathVariable String id) {
        return dispensingService.findById(id);
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "Reintenta un despacho que quedo pendiente por stock")
    public DispensingResponse retry(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        return dispensingService.retry(id, jwt.getSubject());
    }
}
