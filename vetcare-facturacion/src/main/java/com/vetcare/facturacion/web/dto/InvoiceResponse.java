package com.vetcare.facturacion.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.vetcare.facturacion.domain.Invoice;
import com.vetcare.facturacion.domain.InvoiceItem;
import com.vetcare.facturacion.domain.InvoiceStatus;

public record InvoiceResponse(String id, String number, String ownerId, String ownerName, String petId,
        String petName, String appointmentId, LocalDate consultationDate, List<InvoiceItem> items,
        BigDecimal subtotal, BigDecimal taxTotal, BigDecimal total, BigDecimal paid, BigDecimal balance,
        InvoiceStatus status, String statusLabel, Instant issuedAt, String issuedBy, String cancelReason) {

    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(invoice.getId(), invoice.getNumber(), invoice.getOwnerId(), invoice.getOwnerName(),
                invoice.getPetId(), invoice.getPetName(), invoice.getAppointmentId(), invoice.getConsultationDate(),
                invoice.getItems(), invoice.getSubtotal(), invoice.getTaxTotal(), invoice.getTotal(),
                invoice.getPaid(), invoice.getBalance(), invoice.getStatus(), invoice.getStatus().getLabel(),
                invoice.getIssuedAt(), invoice.getIssuedBy(), invoice.getCancelReason());
    }
}
