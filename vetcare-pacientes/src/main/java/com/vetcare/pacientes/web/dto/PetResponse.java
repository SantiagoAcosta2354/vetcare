package com.vetcare.pacientes.web.dto;

import java.time.Instant;
import java.time.LocalDate;

import com.vetcare.pacientes.domain.Pet;
import com.vetcare.pacientes.domain.PetStatus;
import com.vetcare.pacientes.domain.Species;

public record PetResponse(
        String id,
        String ownerId,
        String ownerName,
        String name,
        Species species,
        String breed,
        String sex,
        LocalDate birthDate,
        Double weightKg,
        String microchip,
        PetStatus status,
        String notes,
        Instant createdAt) {

    public static PetResponse from(Pet pet, String ownerName) {
        return new PetResponse(pet.getId(), pet.getOwnerId(), ownerName, pet.getName(), pet.getSpecies(),
                pet.getBreed(), pet.getSex(), pet.getBirthDate(), pet.getWeightKg(), pet.getMicrochip(),
                pet.getStatus(), pet.getNotes(), pet.getCreatedAt());
    }
}
