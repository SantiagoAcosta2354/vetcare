package com.vetcare.farmacia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vetcare.farmacia.domain.Dispensing;
import com.vetcare.farmacia.domain.DispensingStatus;

public interface DispensingRepository extends MongoRepository<Dispensing, String> {

    List<Dispensing> findByStatusOrderByCreatedAtDesc(DispensingStatus status);

    List<Dispensing> findByPetIdOrderByCreatedAtDesc(String petId);

    Optional<Dispensing> findByPrescriptionId(String prescriptionId);
}
