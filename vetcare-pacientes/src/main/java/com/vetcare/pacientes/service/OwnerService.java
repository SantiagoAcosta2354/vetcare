package com.vetcare.pacientes.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.vetcare.pacientes.domain.Owner;
import com.vetcare.pacientes.exception.BusinessRuleException;
import com.vetcare.pacientes.exception.ResourceNotFoundException;
import com.vetcare.pacientes.repository.OwnerRepository;
import com.vetcare.pacientes.web.dto.OwnerRequest;
import com.vetcare.pacientes.web.dto.OwnerResponse;

/**
 * Registro de duenos. El documento identifica a la persona ante la clinica: si
 * se permite duplicarlo, la misma mascota termina con dos historias y dos
 * carteras.
 */
@Service
public class OwnerService {

    private final OwnerRepository ownerRepository;

    public OwnerService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    public List<OwnerResponse> findAll() {
        return ownerRepository.findByActiveTrueOrderByFullNameAsc().stream().map(OwnerResponse::from).toList();
    }

    public List<OwnerResponse> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        String term = query.trim();
        return ownerRepository.findByFullNameContainingIgnoreCaseOrDocumentContainingIgnoreCase(term, term)
                .stream().map(OwnerResponse::from).toList();
    }

    public OwnerResponse findById(String id) {
        return OwnerResponse.from(requireOwner(id));
    }

    public OwnerResponse findByDocument(String document) {
        return OwnerResponse.from(ownerRepository.findByDocument(document.trim())
                .orElseThrow(() -> new ResourceNotFoundException("No existe un dueno con el documento " + document)));
    }

    /**
     * Ficha del dueno autenticado.
     *
     * Si la cuenta aun no esta vinculada, se busca por correo y se vincula: es el
     * caso normal cuando recepcion registra al dueno y despues el dueno crea su
     * cuenta con el mismo correo.
     */
    public OwnerResponse findByUserId(String userId) {
        return OwnerResponse.from(ownerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no tiene ficha de dueno registrada")));
    }

    public OwnerResponse findByUserId(String userId, String email) {
        Owner owner = ownerRepository.findByUserId(userId).orElse(null);
        if (owner == null && email != null && !email.isBlank()) {
            owner = ownerRepository.findByEmailIgnoreCase(email).orElse(null);
            if (owner != null && owner.getUserId() == null) {
                owner.setUserId(userId);
                owner = ownerRepository.save(owner);
            }
        }
        if (owner == null) {
            throw new ResourceNotFoundException("El usuario no tiene ficha de dueno registrada");
        }
        return OwnerResponse.from(owner);
    }

    public OwnerResponse create(OwnerRequest request, String actor) {
        String document = normalizeDocument(request.document());
        if (ownerRepository.existsByDocument(document)) {
            throw new BusinessRuleException("Ya existe un dueno con el documento " + document);
        }
        Owner owner = new Owner();
        apply(owner, request);
        owner.setDocument(document);
        owner.setCreatedBy(actor);
        owner.setActive(true);
        return OwnerResponse.from(ownerRepository.save(owner));
    }

    public OwnerResponse update(String id, OwnerRequest request) {
        Owner owner = requireOwner(id);
        String document = normalizeDocument(request.document());
        ownerRepository.findByDocument(document).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new BusinessRuleException("El documento " + document + " ya pertenece a otro dueno");
            }
        });
        apply(owner, request);
        owner.setDocument(document);
        return OwnerResponse.from(ownerRepository.save(owner));
    }

    public OwnerResponse changeStatus(String id, boolean active) {
        Owner owner = requireOwner(id);
        owner.setActive(active);
        return OwnerResponse.from(ownerRepository.save(owner));
    }

    public Owner requireOwner(String id) {
        return ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un dueno con el id " + id));
    }

    private void apply(Owner owner, OwnerRequest request) {
        owner.setDocumentType(request.documentType());
        owner.setFullName(request.fullName().trim());
        owner.setPhone(trimToNull(request.phone()));
        owner.setEmail(trimToNull(request.email()));
        owner.setAddress(trimToNull(request.address()));
        owner.setUserId(trimToNull(request.userId()));
    }

    private String normalizeDocument(String document) {
        return document == null ? null : document.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
