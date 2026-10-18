package com.vetcare.facturacion.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vetcare.facturacion.domain.Payment;
import com.vetcare.facturacion.domain.PaymentStatus;

public interface PaymentRepository extends MongoRepository<Payment, String> {

    List<Payment> findByInvoiceIdOrderByPaidAtAsc(String invoiceId);

    List<Payment> findByInvoiceIdAndStatus(String invoiceId, PaymentStatus status);
}
