package com.vetcare.clinica.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vetcare.clinica.domain.Prescription;
import com.vetcare.clinica.domain.PrescriptionStatus;

public interface PrescriptionRepository extends MongoRepository<Prescription, String> {

    List<Prescription> findByRecordIdOrderByIssuedAtDesc(String recordId);

    List<Prescription> findByStatusOrderByIssuedAtAsc(PrescriptionStatus status);

    List<Prescription> findByPetIdOrderByIssuedAtDesc(String petId);

    boolean existsByRecordIdAndStatusIn(String recordId, Collection<PrescriptionStatus> statuses);
}
