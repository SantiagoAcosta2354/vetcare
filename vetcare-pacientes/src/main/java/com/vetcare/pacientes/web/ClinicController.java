package com.vetcare.pacientes.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vetcare.pacientes.service.ClinicService;
import com.vetcare.pacientes.web.dto.ClinicRequest;
import com.vetcare.pacientes.web.dto.ClinicResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/clinics")
@Tag(name = "Sedes", description = "Sedes de atencion y su horario")
public class ClinicController {

    private final ClinicService clinicService;

    public ClinicController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @GetMapping
    @Operation(summary = "Lista las sedes activas")
    public List<ClinicResponse> list() {
        return clinicService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta una sede")
    public ClinicResponse findById(@PathVariable String id) {
        return clinicService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra una sede con su horario y duracion de franja")
    public ClinicResponse create(@Valid @RequestBody ClinicRequest request) {
        return clinicService.create(request);
    }
}
