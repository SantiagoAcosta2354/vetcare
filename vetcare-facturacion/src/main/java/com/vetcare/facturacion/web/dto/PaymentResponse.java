package com.vetcare.facturacion.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.vetcare.facturacion.domain.Payment;
import com.vetcare.facturacion.domain.PaymentMethod;
import com.vetcare.facturacion.domain.PaymentStatus;

public record PaymentResponse(String id, String invoiceId, BigDecimal amount, PaymentMethod method, String reference,
        Instant paidAt, String registeredBy, PaymentStatus status, String voidReason) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getInvoiceId(), payment.getAmount(), payment.getMethod(),
                payment.getReference(), payment.getPaidAt(), payment.getRegisteredBy(), payment.getStatus(),
                payment.getVoidReason());
    }
}
