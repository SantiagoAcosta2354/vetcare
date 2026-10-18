package com.vetcare.pacientes.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.vetcare.pacientes.domain.Owner;
import com.vetcare.pacientes.domain.Pet;
import com.vetcare.pacientes.domain.PetStatus;
import com.vetcare.pacientes.exception.BusinessRuleException;
import com.vetcare.pacientes.exception.ResourceNotFoundException;
import com.vetcare.pacientes.repository.PetRepository;
import com.vetcare.pacientes.web.dto.PetRequest;
import com.vetcare.pacientes.web.dto.PetResponse;

/**
 * Registro de pacientes.
 *
 * Reglas: el dueno debe existir y estar activo, la especie es obligatoria y el
 * microchip, cuando se informa, es unico en toda la clinica.
 */
@Service
public class PetService {

    private final PetRepository petRepository;
    private final OwnerService ownerService;

    public PetService(PetRepository petRepository, OwnerService ownerService) {
        this.petRepository = petRepository;
        this.ownerService = ownerService;
    }

    public PetResponse findById(String id) {
        Pet pet = requirePet(id);
        return toResponse(pet);
    }

    public PetResponse findByMicrochip(String microchip) {
        Pet pet = petRepository.findByMicrochip(microchip.trim())
                .orElseThrow(() -> new ResourceNotFoundException("No existe un paciente con el microchip " + microchip));
        return toResponse(pet);
    }

    public List<PetResponse> findByOwner(String ownerId) {
        Owner owner = ownerService.requireOwner(ownerId);
        return petRepository.findByOwnerIdOrderByNameAsc(ownerId).stream()
                .map(pet -> PetResponse.from(pet, owner.getFullName()))
                .toList();
    }

    public List<PetResponse> search(String name) {
        return petRepository.findByNameContainingIgnoreCaseOrderByNameAsc(name).stream()
                .map(this::toResponse)
                .toList();
    }

    public PetResponse create(PetRequest request, String actor) {
        Owner owner = ownerService.requireOwner(request.ownerId());
        if (!owner.isActive()) {
            throw new BusinessRuleException("El dueno esta inactivo; no se pueden registrar pacientes a su nombre");
        }
        String microchip = trimToNull(request.microchip());
        validateMicrochip(microchip, null);

        Pet pet = new Pet();
        apply(pet, request);
        pet.setMicrochip(microchip);
        pet.setStatus(PetStatus.ACTIVO);
        pet.setCreatedBy(actor);
        return PetResponse.from(petRepository.save(pet), owner.getFullName());
    }

    public PetResponse update(String id, PetRequest request) {
        Pet pet = requirePet(id);
        String microchip = trimToNull(request.microchip());
        validateMicrochip(microchip, id);
        apply(pet, request);
        pet.setMicrochip(microchip);
        return toResponse(petRepository.save(pet));
    }

    /**
     * Baja solicitada por el dueno. La historia y las facturas se conservan:
     * solo se impide agendar citas nuevas.
     */
    public PetResponse changeStatus(String id, PetStatus status) {
        Pet pet = requirePet(id);
        pet.setStatus(status);
        return toResponse(petRepository.save(pet));
    }

    public Pet requirePet(String id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un paciente con el id " + id));
    }

    private void apply(Pet pet, PetRequest request) {
        pet.setOwnerId(request.ownerId());
        pet.setName(request.name().trim());
        pet.setSpecies(request.species());
        pet.setBreed(trimToNull(request.breed()));
        pet.setSex(trimToNull(request.sex()));
        pet.setBirthDate(request.birthDate());
        pet.setWeightKg(request.weightKg());
        pet.setNotes(trimToNull(request.notes()));
    }

    private void validateMicrochip(String microchip, String currentPetId) {
        if (microchip == null) {
            return;
        }
        petRepository.findByMicrochip(microchip).ifPresent(other -> {
            if (currentPetId == null || !other.getId().equals(currentPetId)) {
                throw new BusinessRuleException("El microchip " + microchip + " ya esta asignado a otro paciente");
            }
        });
    }

    private PetResponse toResponse(Pet pet) {
        String ownerName = ownerService.requireOwner(pet.getOwnerId()).getFullName();
        return PetResponse.from(pet, ownerName);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
