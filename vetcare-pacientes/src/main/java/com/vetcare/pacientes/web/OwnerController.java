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

import com.vetcare.pacientes.service.OwnerService;
import com.vetcare.pacientes.web.dto.OwnerRequest;
import com.vetcare.pacientes.web.dto.OwnerResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/owners")
@Tag(name = "Duenos", description = "Registro e identificacion de los duenos de mascotas")
public class OwnerController {

    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @GetMapping
    @Operation(summary = "Lista o busca duenos por nombre o documento")
    public List<OwnerResponse> list(@RequestParam(required = false) String q) {
        return ownerService.search(q);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta un dueno por su identificador")
    public OwnerResponse findById(@PathVariable String id) {
        return ownerService.findById(id);
    }

    @GetMapping("/by-document/{document}")
    @Operation(summary = "Consulta un dueno por su documento")
    public OwnerResponse findByDocument(@PathVariable String document) {
        return ownerService.findByDocument(document);
    }

    @GetMapping("/by-user/{userId}")
    @Operation(summary = "Consulta la ficha de dueno asociada a una cuenta del portal")
    public OwnerResponse findByUser(@PathVariable String userId) {
        return ownerService.findByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un dueno; el documento es unico")
    public OwnerResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody OwnerRequest request) {
        return ownerService.create(request, jwt.getSubject());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza los datos de un dueno")
    public OwnerResponse update(@PathVariable String id, @Valid @RequestBody OwnerRequest request) {
        return ownerService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activa o desactiva un dueno")
    public OwnerResponse changeStatus(@PathVariable String id, @RequestParam boolean active) {
        return ownerService.changeStatus(id, active);
    }
}
