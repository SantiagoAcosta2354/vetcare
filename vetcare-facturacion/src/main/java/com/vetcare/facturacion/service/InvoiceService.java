package com.vetcare.facturacion.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vetcare.facturacion.client.PacientesClient;
import com.vetcare.facturacion.domain.Invoice;
import com.vetcare.facturacion.domain.InvoiceItem;
import com.vetcare.facturacion.domain.InvoiceStatus;
import com.vetcare.facturacion.domain.ItemType;
import com.vetcare.facturacion.domain.Payment;
import com.vetcare.facturacion.domain.PaymentStatus;
import com.vetcare.facturacion.exception.BusinessRuleException;
import com.vetcare.facturacion.exception.ResourceNotFoundException;
import com.vetcare.facturacion.repository.InvoiceRepository;
import com.vetcare.facturacion.repository.PaymentRepository;
import com.vetcare.facturacion.web.dto.InvoiceRequest;
import com.vetcare.facturacion.web.dto.InvoiceResponse;
import com.vetcare.facturacion.web.dto.PaymentRequest;
import com.vetcare.facturacion.web.dto.PaymentResponse;
import com.vetcare.facturacion.web.dto.PortfolioResponse;

/**
 * Facturacion.
 *
 * Reglas:
 *   1. La consulta del dia no se cobra dos veces al mismo paciente.
 *   2. El IVA solo se aplica a productos.
 *   3. La factura pasa a PAGADA unicamente cuando el saldo llega a cero.
 *   4. Un pago no se edita: se anula con motivo y el saldo se recalcula.
 *   5. Anular exige motivo y no hay pagos aplicados sin revertir.
 */
@Service
public class InvoiceService {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PacientesClient pacientesClient;

    @Value("${vetcare.billing.default-tax-rate:0.19}")
    private BigDecimal defaultTaxRate;

    public InvoiceService(InvoiceRepository invoiceRepository, PaymentRepository paymentRepository,
            PacientesClient pacientesClient) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.pacientesClient = pacientesClient;
    }

    // ------------------------------------------------------------- emision

    public InvoiceResponse issue(InvoiceRequest request, String actor) {
        boolean hasConsultation = request.items().stream()
                .anyMatch(item -> "CONSULTA".equalsIgnoreCase(item.code()));

        if (hasConsultation && alreadyBilled(request.petId(), request.consultationDate())) {
            throw new BusinessRuleException("La consulta de " + request.petName() + " del "
                    + request.consultationDate() + " ya fue facturada");
        }

        Invoice invoice = new Invoice();
        invoice.setNumber(nextNumber());
        invoice.setOwnerId(request.ownerId());
        invoice.setOwnerName(request.ownerName());
        invoice.setPetId(request.petId());
        invoice.setPetName(request.petName());
        invoice.setAppointmentId(request.appointmentId());
        invoice.setConsultationDate(request.consultationDate());
        invoice.setIssuedAt(Instant.now());
        invoice.setIssuedBy(actor);
        invoice.setStatus(InvoiceStatus.EMITIDA);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        List<InvoiceItem> items = new ArrayList<>();

        for (InvoiceRequest.Item requested : request.items()) {
            InvoiceItem item = new InvoiceItem();
            item.setCode(requested.code().toUpperCase());
            item.setDescription(requested.description().trim());
            item.setType(requested.type());
            item.setReferenceId(requested.referenceId());
            item.setQuantity(requested.quantity());
            item.setUnitPrice(scale(requested.unitPrice()));

            BigDecimal lineSubtotal = scale(item.getUnitPrice().multiply(BigDecimal.valueOf(requested.quantity())));
            BigDecimal taxRate = requested.type() == ItemType.PRODUCTO ? defaultTaxRate : BigDecimal.ZERO;
            BigDecimal tax = scale(lineSubtotal.multiply(taxRate));

            item.setTaxRate(taxRate);
            item.setSubtotal(lineSubtotal);
            item.setTax(tax);
            item.setTotal(scale(lineSubtotal.add(tax)));

            subtotal = subtotal.add(lineSubtotal);
            taxTotal = taxTotal.add(tax);
            items.add(item);
        }

        invoice.setItems(items);
        invoice.setSubtotal(scale(subtotal));
        invoice.setTaxTotal(scale(taxTotal));
        invoice.setTotal(scale(subtotal.add(taxTotal)));
        invoice.setPaid(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        invoice.setBalance(invoice.getTotal());

        return InvoiceResponse.from(invoiceRepository.save(invoice));
    }

    private boolean alreadyBilled(String petId, LocalDate date) {
        return invoiceRepository.findByPetIdAndConsultationDateAndStatusNot(petId, date, InvoiceStatus.ANULADA)
                .stream()
                .anyMatch(invoice -> invoice.getItems().stream()
                        .anyMatch(item -> "CONSULTA".equalsIgnoreCase(item.getCode())));
    }

    // --------------------------------------------------------------- pagos

    public PaymentResponse addPayment(String invoiceId, PaymentRequest request, String actor) {
        Invoice invoice = requireInvoice(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.EMITIDA) {
            throw new BusinessRuleException("Solo se abonan facturas emitidas; esta factura esta "
                    + invoice.getStatus().getLabel().toLowerCase());
        }
        BigDecimal amount = scale(request.amount());
        if (amount.compareTo(invoice.getBalance()) > 0) {
            throw new BusinessRuleException("El pago supera el saldo pendiente de " + invoice.getBalance());
        }

        Payment payment = new Payment();
        payment.setInvoiceId(invoiceId);
        payment.setAmount(amount);
        payment.setMethod(request.method());
        payment.setReference(request.reference());
        payment.setPaidAt(Instant.now());
        payment.setRegisteredBy(actor);
        payment.setStatus(PaymentStatus.APLICADO);
        Payment saved = paymentRepository.save(payment);

        invoice.setPaid(scale(invoice.getPaid().add(amount)));
        invoice.setBalance(scale(invoice.getTotal().subtract(invoice.getPaid())));
        if (invoice.getBalance().compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(InvoiceStatus.PAGADA);
        }
        invoiceRepository.save(invoice);
        return PaymentResponse.from(saved);
    }

    public PaymentResponse voidPayment(String paymentId, String reason, String actor) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un pago con el id " + paymentId));
        if (payment.getStatus() == PaymentStatus.ANULADO) {
            throw new BusinessRuleException("El pago ya esta anulado");
        }
        Invoice invoice = requireInvoice(payment.getInvoiceId());
        if (invoice.getStatus() == InvoiceStatus.ANULADA) {
            throw new BusinessRuleException("La factura esta anulada; no se modifica el pago");
        }

        payment.setStatus(PaymentStatus.ANULADO);
        payment.setVoidReason(reason.trim());
        payment.setVoidedBy(actor);
        payment.setVoidedAt(Instant.now());
        paymentRepository.save(payment);

        invoice.setPaid(scale(invoice.getPaid().subtract(payment.getAmount())));
        invoice.setBalance(scale(invoice.getTotal().subtract(invoice.getPaid())));
        if (invoice.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(InvoiceStatus.EMITIDA);
        }
        invoiceRepository.save(invoice);
        return PaymentResponse.from(payment);
    }

    public List<PaymentResponse> paymentsOf(String invoiceId) {
        requireInvoice(invoiceId);
        return paymentRepository.findByInvoiceIdOrderByPaidAtAsc(invoiceId).stream()
                .map(PaymentResponse::from).toList();
    }

    // ------------------------------------------------------------ anulacion

    public InvoiceResponse cancel(String invoiceId, String reason, String actor) {
        Invoice invoice = requireInvoice(invoiceId);
        if (invoice.getStatus() == InvoiceStatus.ANULADA) {
            throw new BusinessRuleException("La factura ya esta anulada");
        }
        boolean hasAppliedPayments = paymentRepository
                .findByInvoiceIdAndStatus(invoiceId, PaymentStatus.APLICADO).stream()
                .anyMatch(payment -> payment.getAmount().compareTo(BigDecimal.ZERO) > 0);
        if (hasAppliedPayments) {
            throw new BusinessRuleException("La factura tiene pagos aplicados; anule primero los pagos");
        }

        invoice.setStatus(InvoiceStatus.ANULADA);
        invoice.setCancelReason(reason.trim());
        invoice.setCancelledBy(actor);
        invoice.setCancelledAt(Instant.now());
        return InvoiceResponse.from(invoiceRepository.save(invoice));
    }

    // ------------------------------------------------------------ consultas

    public InvoiceResponse findById(String id) {
        return InvoiceResponse.from(requireInvoice(id));
    }

    public InvoiceResponse findByNumber(String number) {
        return InvoiceResponse.from(invoiceRepository.findByNumber(number)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una factura con el numero " + number)));
    }

    public List<InvoiceResponse> list(InvoiceStatus status, String ownerId, String petId) {
        if (petId != null && !petId.isBlank()) {
            return invoiceRepository.findByPetIdOrderByIssuedAtDesc(petId).stream().map(InvoiceResponse::from).toList();
        }
        if (ownerId != null && !ownerId.isBlank()) {
            return invoiceRepository.findByOwnerIdOrderByIssuedAtDesc(ownerId).stream().map(InvoiceResponse::from).toList();
        }
        if (status != null) {
            return invoiceRepository.findByStatusOrderByIssuedAtDesc(status).stream().map(InvoiceResponse::from).toList();
        }
        return invoiceRepository.findAll().stream().map(InvoiceResponse::from).toList();
    }

    public List<InvoiceResponse> forOwnerFromPortal() {
        PacientesClient.OwnerData owner = pacientesClient.ownerFromPortal();
        return invoiceRepository.findByOwnerIdOrderByIssuedAtDesc(owner.id()).stream()
                .map(InvoiceResponse::from).toList();
    }

    public PortfolioResponse portfolio() {
        List<Invoice> invoices = invoiceRepository.findAll();
        BigDecimal billed = BigDecimal.ZERO;
        BigDecimal collected = BigDecimal.ZERO;
        BigDecimal pending = BigDecimal.ZERO;
        long count = 0;

        for (Invoice invoice : invoices) {
            if (invoice.getStatus() == InvoiceStatus.ANULADA) {
                continue;
            }
            count++;
            billed = billed.add(invoice.getTotal());
            collected = collected.add(invoice.getPaid());
            pending = pending.add(invoice.getBalance());
        }
        return new PortfolioResponse(count, scale(billed), scale(collected), scale(pending));
    }

    public Invoice requireInvoice(String id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una factura con el id " + id));
    }

    // ---------------------------------------------------------------- apoyo

    private String nextNumber() {
        long sequence = invoiceRepository.count() + 1;
        return String.format("FV-%06d", sequence);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
