package com.vetcare.clinica.web;

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

import com.vetcare.clinica.service.VaccinationService;
import com.vetcare.clinica.web.dto.VaccinationResponse;
import com.vetcare.clinica.web.dto.VaccineRequest;
import com.vetcare.clinica.web.dto.VaccineResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Vacunacion", description = "Catalogo de vacunas, aplicacion y proximas dosis")
public class VaccinationController {

    private final VaccinationService vaccinationService;

    public VaccinationController(VaccinationService vaccinationService) {
        this.vaccinationService = vaccinationService;
    }

    @GetMapping("/vaccines")
    @Operation(summary = "Catalogo de vacunas activas con su esquema")
    public List<VaccineResponse> vaccines() {
        return vaccinationService.vaccines();
    }

    @PostMapping("/vaccines")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Agrega una vacuna al catalogo")
    public VaccineResponse createVaccine(@Valid @RequestBody VaccineRequest request) {
        return vaccinationService.createVaccine(request);
    }

    @GetMapping("/vaccinations")
    @Operation(summary = "Vacunas aplicadas a un paciente")
    public List<VaccinationResponse> byPet(@RequestParam String petId) {
        return vaccinationService.findByPet(petId);
    }

    @GetMapping("/vaccinations/upcoming")
    @Operation(summary = "Proximas dosis en los siguientes dias (tablero)")
    public List<VaccinationResponse> upcoming(@RequestParam(defaultValue = "30") int days) {
        return vaccinationService.upcoming(days);
    }

    @GetMapping("/vaccinations/portal/{petId}")
    @Operation(summary = "Proximas dosis de una mascota del dueno autenticado")
    public List<VaccinationResponse> forOwner(@AuthenticationPrincipal Jwt jwt, @PathVariable String petId) {
        return vaccinationService.forOwnerPet(petId);
    }
}
