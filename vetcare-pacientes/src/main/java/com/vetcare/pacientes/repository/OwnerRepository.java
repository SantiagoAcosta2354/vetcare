package com.vetcare.pacientes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vetcare.pacientes.domain.Owner;

public interface OwnerRepository extends MongoRepository<Owner, String> {

    Optional<Owner> findByDocument(String document);

    boolean existsByDocument(String document);

    Optional<Owner> findByUserId(String userId);

    Optional<Owner> findByEmailIgnoreCase(String email);

    List<Owner> findByActiveTrueOrderByFullNameAsc();

    List<Owner> findByFullNameContainingIgnoreCaseOrDocumentContainingIgnoreCase(String fullName, String document);
}
