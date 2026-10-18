package com.vetcare.facturacion.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vetcare.facturacion.domain.InvoiceStatus;
import com.vetcare.facturacion.service.InvoiceService;
import com.vetcare.facturacion.web.dto.InvoiceRequest;
import com.vetcare.facturacion.web.dto.InvoiceResponse;
import com.vetcare.facturacion.web.dto.PaymentRequest;
import com.vetcare.facturacion.web.dto.PaymentResponse;
import com.vetcare.facturacion.web.dto.PortfolioResponse;
import com.vetcare.facturacion.web.dto.ReasonRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Facturacion", description = "Emision, pagos, anulacion y cartera")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping("/invoices")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Emite una factura con servicios y productos")
    public InvoiceResponse issue(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody InvoiceRequest request) {
        return invoiceService.issue(request, jwt.getSubject());
    }

    @GetMapping("/invoices")
    @Operation(summary = "Facturas por estado, dueno o paciente")
    public List<InvoiceResponse> list(@RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) String ownerId,
            @RequestParam(required = false) String petId) {
        return invoiceService.list(status, ownerId, petId);
    }

    @GetMapping("/invoices/{id}")
    @Operation(summary = "Consulta una factura")
    public InvoiceResponse findById(@PathVariable String id) {
        return invoiceService.findById(id);
    }

    @GetMapping("/invoices/by-number/{number}")
    @Operation(summary = "Consulta una factura por su numero")
    public InvoiceResponse findByNumber(@PathVariable String number) {
        return invoiceService.findByNumber(number);
    }

    @PostMapping("/invoices/{id}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un pago parcial o total")
    public PaymentResponse pay(@AuthenticationPrincipal Jwt jwt, @PathVariable String id,
            @Valid @RequestBody PaymentRequest request) {
        return invoiceService.addPayment(id, request, jwt.getSubject());
    }

    @GetMapping("/invoices/{id}/payments")
    @Operation(summary = "Pagos registrados en una factura")
    public List<PaymentResponse> payments(@PathVariable String id) {
        return invoiceService.paymentsOf(id);
    }

    @PostMapping("/payments/{id}/void")
    @Operation(summary = "Anula un pago con motivo y recalcula el saldo")
    public PaymentResponse voidPayment(@AuthenticationPrincipal Jwt jwt, @PathVariable String id,
            @Valid @RequestBody ReasonRequest request) {
        return invoiceService.voidPayment(id, request.reason(), jwt.getSubject());
    }

    @PostMapping("/invoices/{id}/cancel")
    @Operation(summary = "Anula una factura con motivo (solo administrador)")
    public InvoiceResponse cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable String id,
            @Valid @RequestBody ReasonRequest request) {
        return invoiceService.cancel(id, request.reason(), jwt.getSubject());
    }

    @GetMapping("/portfolio")
    @Operation(summary = "Cartera: facturado, cobrado y pendiente")
    public PortfolioResponse portfolio() {
        return invoiceService.portfolio();
    }

    @GetMapping("/portal/invoices")
    @Operation(summary = "Facturas del dueno autenticado (portal)")
    public List<InvoiceResponse> portalInvoices() {
        return invoiceService.forOwnerFromPortal();
    }
}
