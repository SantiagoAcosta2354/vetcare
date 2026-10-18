package com.vetcare.pacientes.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vetcare.pacientes.domain.Clinic;

public interface ClinicRepository extends MongoRepository<Clinic, String> {

    List<Clinic> findByActiveTrueOrderByNameAsc();
}
