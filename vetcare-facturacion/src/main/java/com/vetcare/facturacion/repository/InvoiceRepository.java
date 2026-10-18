package com.vetcare.facturacion.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vetcare.facturacion.domain.Invoice;
import com.vetcare.facturacion.domain.InvoiceStatus;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {

    Optional<Invoice> findByNumber(String number);

    List<Invoice> findByStatusOrderByIssuedAtDesc(InvoiceStatus status);

    List<Invoice> findByOwnerIdOrderByIssuedAtDesc(String ownerId);

    List<Invoice> findByPetIdOrderByIssuedAtDesc(String petId);

    List<Invoice> findByConsultationDateOrderByIssuedAtDesc(LocalDate date);

    List<Invoice> findByPetIdAndConsultationDateAndStatusNot(String petId, LocalDate date, InvoiceStatus status);

    List<Invoice> findByStatus(InvoiceStatus status);
}
