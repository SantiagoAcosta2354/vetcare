package com.vetcare.clinica.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vetcare.clinica.domain.MedicalRecord;

public interface MedicalRecordRepository extends MongoRepository<MedicalRecord, String> {

    Optional<MedicalRecord> findByAppointmentId(String appointmentId);

    boolean existsByAppointmentId(String appointmentId);

    List<MedicalRecord> findByPetIdOrderByOpenedAtDesc(String petId);
}
