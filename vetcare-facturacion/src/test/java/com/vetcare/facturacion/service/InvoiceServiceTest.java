package com.vetcare.facturacion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.vetcare.facturacion.client.PacientesClient;
import com.vetcare.facturacion.domain.Invoice;
import com.vetcare.facturacion.domain.InvoiceItem;
import com.vetcare.facturacion.domain.InvoiceStatus;
import com.vetcare.facturacion.domain.ItemType;
import com.vetcare.facturacion.domain.Payment;
import com.vetcare.facturacion.domain.PaymentMethod;
import com.vetcare.facturacion.domain.PaymentStatus;
import com.vetcare.facturacion.exception.BusinessRuleException;
import com.vetcare.facturacion.repository.InvoiceRepository;
import com.vetcare.facturacion.repository.PaymentRepository;
import com.vetcare.facturacion.web.dto.InvoiceRequest;
import com.vetcare.facturacion.web.dto.InvoiceResponse;
import com.vetcare.facturacion.web.dto.PaymentRequest;
import com.vetcare.facturacion.web.dto.PaymentResponse;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PacientesClient pacientesClient;

    private InvoiceService service;

    @BeforeEach
    void setUp() {
        service = new InvoiceService(invoiceRepository, paymentRepository, pacientesClient);
        ReflectionTestUtils.setField(service, "defaultTaxRate", new BigDecimal("0.19"));
    }

    private InvoiceRequest request(String code, ItemType type, String unitPrice, int quantity) {
        return new InvoiceRequest("o1", "Ana Perez", "p1", "Luna", "a1", LocalDate.now(),
                List.of(new InvoiceRequest.Item(code, "Item de prueba", type, null, quantity, new BigDecimal(unitPrice))));
    }

    private Invoice invoice(String status, String total, String paid) {
        Invoice invoice = new Invoice();
        invoice.setId("i1");
        invoice.setNumber("FV-000001");
        invoice.setPetId("p1");
        invoice.setTotal(new BigDecimal(total));
        invoice.setPaid(new BigDecimal(paid));
        invoice.setBalance(new BigDecimal(total).subtract(new BigDecimal(paid)));
        invoice.setStatus(InvoiceStatus.valueOf(status));
        return invoice;
    }

    @Test
    @DisplayName("El IVA se aplica solo a productos, nunca a servicios")
    void ivaSoloEnProductos() {
        when(invoiceRepository.findByPetIdAndConsultationDateAndStatusNot(anyString(), any(), any()))
                .thenReturn(List.of());
        when(invoiceRepository.count()).thenReturn(0L);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceRequest request = new InvoiceRequest("o1", "Ana Perez", "p1", "Luna", "a1", LocalDate.now(),
                List.of(
                        new InvoiceRequest.Item("CONSULTA", "Consulta general", ItemType.SERVICIO, null, 1, new BigDecimal("80000")),
                        new InvoiceRequest.Item("MED-1", "Amoxicilina", ItemType.PRODUCTO, "d1", 1, new BigDecimal("10000"))));

        InvoiceResponse response = service.issue(request, "u1");

        assertThat(response.items().get(0).getTax()).isEqualByComparingTo("0.00");
        assertThat(response.items().get(1).getTax()).isEqualByComparingTo("1900.00");
        assertThat(response.taxTotal()).isEqualByComparingTo("1900.00");
        assertThat(response.total()).isEqualByComparingTo("91900.00");
        assertThat(response.balance()).isEqualByComparingTo("91900.00");
        assertThat(response.status()).isEqualTo(InvoiceStatus.EMITIDA);
    }

    @Test
    @DisplayName("La consulta del dia no se factura dos veces al mismo paciente")
    void consultaDuplicadaSeRechaza() {
        Invoice existing = invoice("EMITIDA", "80000", "0");
        InvoiceItem consultation = new InvoiceItem();
        consultation.setCode("CONSULTA");
        existing.setItems(List.of(consultation));
        when(invoiceRepository.findByPetIdAndConsultationDateAndStatusNot(eq("p1"), any(), eq(InvoiceStatus.ANULADA)))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> service.issue(request("CONSULTA", ItemType.SERVICIO, "80000", 1), "u1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya fue facturada");

        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Un pago parcial deja la factura emitida con saldo")
    void pagoParcialNoMarcaPagada() {
        Invoice invoice = invoice("EMITIDA", "80000", "0");
        when(invoiceRepository.findById("i1")).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse payment = service.addPayment("i1",
                new PaymentRequest(new BigDecimal("30000"), PaymentMethod.EFECTIVO, null), "u1");

        assertThat(payment.status()).isEqualTo(PaymentStatus.APLICADO);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.EMITIDA);
        assertThat(invoice.getBalance()).isEqualByComparingTo("50000.00");
    }

    @Test
    @DisplayName("Cuando el saldo llega a cero la factura queda pagada")
    void pagoTotalMarcaPagada() {
        Invoice invoice = invoice("EMITIDA", "80000", "0");
        when(invoiceRepository.findById("i1")).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.addPayment("i1", new PaymentRequest(new BigDecimal("80000"), PaymentMethod.TARJETA, "1234"), "u1");

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAGADA);
        assertThat(invoice.getBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Un pago no puede superar el saldo pendiente")
    void pagoSuperaSaldoSeRechaza() {
        Invoice invoice = invoice("EMITIDA", "80000", "0");
        when(invoiceRepository.findById("i1")).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> service.addPayment("i1",
                new PaymentRequest(new BigDecimal("90000"), PaymentMethod.EFECTIVO, null), "u1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("supera el saldo");

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Anular un pago devuelve la factura a emitida y restablece el saldo")
    void anularPagoRestableceSaldo() {
        Invoice invoice = invoice("PAGADA", "80000", "80000");
        Payment payment = new Payment();
        payment.setId("pay1");
        payment.setInvoiceId("i1");
        payment.setAmount(new BigDecimal("80000"));
        payment.setStatus(PaymentStatus.APLICADO);

        when(paymentRepository.findById("pay1")).thenReturn(Optional.of(payment));
        when(invoiceRepository.findById("i1")).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.voidPayment("pay1", "Pago duplicado en datafono", "u1");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.ANULADO);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.EMITIDA);
        assertThat(invoice.getBalance()).isEqualByComparingTo("80000.00");
    }

    @Test
    @DisplayName("No se anula una factura con pagos aplicados")
    void anularConPagosSeRechaza() {
        Invoice invoice = invoice("EMITIDA", "80000", "30000");
        Payment payment = new Payment();
        payment.setAmount(new BigDecimal("30000"));
        payment.setStatus(PaymentStatus.APLICADO);

        when(invoiceRepository.findById("i1")).thenReturn(Optional.of(invoice));
        when(paymentRepository.findByInvoiceIdAndStatus("i1", PaymentStatus.APLICADO))
                .thenReturn(List.of(payment));

        assertThatThrownBy(() -> service.cancel("i1", "Error de digitacion", "admin-1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("anule primero los pagos");
    }

    @Test
    @DisplayName("Anular una factura sin pagos exige motivo y la deja anulada")
    void anularSinPagos() {
        Invoice invoice = invoice("EMITIDA", "80000", "0");
        when(invoiceRepository.findById("i1")).thenReturn(Optional.of(invoice));
        when(paymentRepository.findByInvoiceIdAndStatus("i1", PaymentStatus.APLICADO)).thenReturn(List.of());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceResponse response = service.cancel("i1", "Factura duplicada", "admin-1");

        assertThat(response.status()).isEqualTo(InvoiceStatus.ANULADA);
        assertThat(response.cancelReason()).isEqualTo("Factura duplicada");
    }
}
