package com.vetcare.farmacia.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vetcare.farmacia.domain.Batch;

public interface BatchRepository extends MongoRepository<Batch, String> {

    List<Batch> findByMedicationIdOrderByExpiresAtAsc(String medicationId);

    /** Lotes vigentes: la fecha de vencimiento es la que habilita el despacho. */
    List<Batch> findByMedicationIdAndExpiresAtGreaterThanEqualAndQuantityGreaterThanOrderByExpiresAtAsc(
            String medicationId, LocalDate today, int quantity);

    Optional<Batch> findByMedicationIdAndBatchNumber(String medicationId, String batchNumber);

    List<Batch> findByQuantityGreaterThanAndExpiresAtLessThanEqualOrderByExpiresAtAsc(int quantity, LocalDate limit);
}
