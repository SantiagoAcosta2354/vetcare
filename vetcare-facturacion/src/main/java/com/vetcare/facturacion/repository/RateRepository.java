package com.vetcare.facturacion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vetcare.facturacion.domain.Rate;

public interface RateRepository extends MongoRepository<Rate, String> {

    List<Rate> findByActiveTrueOrderByNameAsc();

    Optional<Rate> findByCode(String code);
}
