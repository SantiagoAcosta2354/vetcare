package com.vetcare.usuarios.web.dto;

import java.time.Instant;
import java.util.List;

import com.vetcare.usuarios.domain.AuditEntry;
import com.vetcare.usuarios.domain.Role;
import com.vetcare.usuarios.domain.User;

/** Vista publica de una cuenta. Nunca incluye el hash de la contrasena. */
public record UserResponse(
        String id,
        String fullName,
        String email,
        String phone,
        List<String> roles,
        List<String> roleLabels,
        boolean active,
        Instant lastLoginAt,
        Instant createdAt,
        Instant updatedAt,
        List<AuditEntry> audit) {

    public static UserResponse from(User user) {
        List<String> roles = user.getRoles() == null ? List.of() : user.getRoles().stream().map(Role::name).sorted().toList();
        List<String> labels = user.getRoles() == null ? List.of() : user.getRoles().stream().map(Role::getLabel).sorted().toList();
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getPhone(), roles, labels,
                user.isActive(), user.getLastLoginAt(), user.getCreatedAt(), user.getUpdatedAt(), user.getAudit());
    }
}
