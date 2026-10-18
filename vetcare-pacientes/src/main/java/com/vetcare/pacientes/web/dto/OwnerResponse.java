package com.vetcare.pacientes.web.dto;

import java.time.Instant;

import com.vetcare.pacientes.domain.DocumentType;
import com.vetcare.pacientes.domain.Owner;

public record OwnerResponse(
        String id,
        DocumentType documentType,
        String document,
        String fullName,
        String phone,
        String email,
        String address,
        String userId,
        boolean active,
        Instant createdAt) {

    public static OwnerResponse from(Owner owner) {
        return new OwnerResponse(owner.getId(), owner.getDocumentType(), owner.getDocument(), owner.getFullName(),
                owner.getPhone(), owner.getEmail(), owner.getAddress(), owner.getUserId(), owner.isActive(),
                owner.getCreatedAt());
    }
}
