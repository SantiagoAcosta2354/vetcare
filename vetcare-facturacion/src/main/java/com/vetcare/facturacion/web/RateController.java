package com.vetcare.facturacion.web;

import java.util.List;

import org.springframework.http.HttpStatus;
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

import com.vetcare.facturacion.service.RateService;
import com.vetcare.facturacion.web.dto.RateRequest;
import com.vetcare.facturacion.web.dto.RateResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/rates")
@Tag(name = "Tarifas", description = "Catalogo de precios administrable")
public class RateController {

    private final RateService rateService;

    public RateController(RateService rateService) {
        this.rateService = rateService;
    }

    @GetMapping
    @Operation(summary = "Lista las tarifas activas")
    public List<RateResponse> list() {
        return rateService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea una tarifa (solo administrador)")
    public RateResponse create(@Valid @RequestBody RateRequest request) {
        return rateService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza una tarifa (solo administrador)")
    public RateResponse update(@PathVariable String id, @Valid @RequestBody RateRequest request) {
        return rateService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activa o retira una tarifa")
    public RateResponse changeStatus(@PathVariable String id, @RequestParam boolean active) {
        return rateService.changeStatus(id, active);
    }
}
