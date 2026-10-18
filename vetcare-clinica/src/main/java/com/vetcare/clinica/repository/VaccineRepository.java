package com.vetcare.clinica.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vetcare.clinica.domain.Vaccine;

public interface VaccineRepository extends MongoRepository<Vaccine, String> {

    List<Vaccine> findByActiveTrueOrderByNameAsc();
}
