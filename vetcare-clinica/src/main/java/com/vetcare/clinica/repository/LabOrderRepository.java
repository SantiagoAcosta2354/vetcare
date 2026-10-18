package com.vetcare.clinica.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vetcare.clinica.domain.LabOrder;
import com.vetcare.clinica.domain.LabStatus;

public interface LabOrderRepository extends MongoRepository<LabOrder, String> {

    List<LabOrder> findByStatusOrderByOrderedAtAsc(LabStatus status);

    List<LabOrder> findByRecordId(String recordId);

    List<LabOrder> findByPetIdOrderByOrderedAtDesc(String petId);
}
