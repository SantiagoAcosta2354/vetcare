package com.vetcare.pacientes.web;

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

import com.vetcare.pacientes.domain.PetStatus;
import com.vetcare.pacientes.service.PetService;
import com.vetcare.pacientes.web.dto.PetRequest;
import com.vetcare.pacientes.web.dto.PetResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/pets")
@Tag(name = "Pacientes", description = "Mascotas registradas y su estado")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    @Operation(summary = "Lista los pacientes de un dueno o busca por nombre")
    public List<PetResponse> list(@RequestParam(required = false) String ownerId,
            @RequestParam(required = false) String q) {
        if (q != null && !q.isBlank()) {
            return petService.search(q);
        }
        return petService.findByOwner(ownerId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta un paciente por su identificador")
    public PetResponse findById(@PathVariable String id) {
        return petService.findById(id);
    }

    @GetMapping("/by-microchip/{microchip}")
    @Operation(summary = "Consulta un paciente por su microchip")
    public PetResponse findByMicrochip(@PathVariable String microchip) {
        return petService.findByMicrochip(microchip);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra una mascota para un dueno existente")
    public PetResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PetRequest request) {
        return petService.create(request, jwt.getSubject());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza la ficha del paciente")
    public PetResponse update(@PathVariable String id, @Valid @RequestBody PetRequest request) {
        return petService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Suspende o reactiva un paciente por solicitud del dueno")
    public PetResponse changeStatus(@PathVariable String id, @RequestParam PetStatus status) {
        return petService.changeStatus(id, status);
    }
}
