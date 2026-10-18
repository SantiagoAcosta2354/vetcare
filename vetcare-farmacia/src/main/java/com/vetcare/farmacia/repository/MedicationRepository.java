package com.vetcare.farmacia.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vetcare.farmacia.domain.Medication;

public interface MedicationRepository extends MongoRepository<Medication, String> {

    List<Medication> findByActiveTrueOrderByNameAsc();

    List<Medication> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    List<Medication> findByNameIgnoreCase(String name);
}
