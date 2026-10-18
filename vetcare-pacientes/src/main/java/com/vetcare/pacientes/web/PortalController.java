package com.vetcare.pacientes.web;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vetcare.pacientes.service.AppointmentService;
import com.vetcare.pacientes.service.OwnerService;
import com.vetcare.pacientes.service.PetService;
import com.vetcare.pacientes.web.dto.AppointmentResponse;
import com.vetcare.pacientes.web.dto.OwnerResponse;
import com.vetcare.pacientes.web.dto.PetResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Rutas del portal del dueno. El filtro no llega por parametro: se resuelve con
 * el claim "sub" del token, de modo que un dueno no puede pedir las mascotas de
 * otro cambiando un identificador en la URL.
 */
@RestController
@RequestMapping("/api/v1/portal")
@Tag(name = "Portal del dueno", description = "Consultas acotadas al dueno autenticado")
public class PortalController {

    private final OwnerService ownerService;
    private final PetService petService;
    private final AppointmentService appointmentService;

    public PortalController(OwnerService ownerService, PetService petService, AppointmentService appointmentService) {
        this.ownerService = ownerService;
        this.petService = petService;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/owner")
    @Operation(summary = "Ficha de dueno asociada a la cuenta autenticada")
    public OwnerResponse owner(@AuthenticationPrincipal Jwt jwt) {
        return ownerService.findByUserId(jwt.getSubject());
    }

    @GetMapping("/pets")
    @Operation(summary = "Mascotas del dueno autenticado")
    public List<PetResponse> pets(@AuthenticationPrincipal Jwt jwt) {
        OwnerResponse owner = ownerService.findByUserId(jwt.getSubject());
        return petService.findByOwner(owner.id());
    }

    @GetMapping("/appointments")
    @Operation(summary = "Citas del dueno autenticado")
    public List<AppointmentResponse> appointments(@AuthenticationPrincipal Jwt jwt) {
        OwnerResponse owner = ownerService.findByUserId(jwt.getSubject());
        return appointmentService.findByOwner(owner.id());
    }
}
