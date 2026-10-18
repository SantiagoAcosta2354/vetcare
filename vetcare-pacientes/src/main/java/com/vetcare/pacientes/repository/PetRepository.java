package com.vetcare.pacientes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vetcare.pacientes.domain.Pet;

public interface PetRepository extends MongoRepository<Pet, String> {

    List<Pet> findByOwnerIdOrderByNameAsc(String ownerId);

    Optional<Pet> findByMicrochip(String microchip);

    boolean existsByMicrochip(String microchip);

    List<Pet> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
}
