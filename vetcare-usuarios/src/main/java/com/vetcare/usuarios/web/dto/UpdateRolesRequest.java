package com.vetcare.usuarios.web.dto;

import java.util.Set;

import com.vetcare.usuarios.domain.Role;

import jakarta.validation.constraints.NotEmpty;

public record UpdateRolesRequest(@NotEmpty(message = "La cuenta debe conservar al menos un rol") Set<Role> roles) {
}
