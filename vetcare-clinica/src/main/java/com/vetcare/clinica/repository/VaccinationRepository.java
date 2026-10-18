package com.vetcare.clinica.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vetcare.clinica.domain.Vaccination;

public interface VaccinationRepository extends MongoRepository<Vaccination, String> {

    List<Vaccination> findByPetIdOrderByAppliedAtDesc(String petId);

    List<Vaccination> findByNextDoseAtBetweenOrderByNextDoseAtAsc(LocalDate from, LocalDate to);

    List<Vaccination> findByRecordId(String recordId);
}
