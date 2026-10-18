package com.vetcare.farmacia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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

import com.vetcare.farmacia.domain.Batch;
import com.vetcare.farmacia.domain.Medication;
import com.vetcare.farmacia.exception.BusinessRuleException;
import com.vetcare.farmacia.repository.BatchRepository;
import com.vetcare.farmacia.repository.MedicationRepository;
import com.vetcare.farmacia.web.dto.PurchaseRequest;

@ExtendWith(MockitoExtension.class)
class MedicationServiceTest {

    @Mock
    private MedicationRepository medicationRepository;
    @Mock
    private BatchRepository batchRepository;

    private MedicationService service;

    @BeforeEach
    void setUp() {
        service = new MedicationService(medicationRepository, batchRepository);
        ReflectionTestUtils.setField(service, "alertDays", 30);
    }

    private Medication medication() {
        Medication medication = new Medication();
        medication.setId("m1");
        medication.setName("Amoxicilina 250 mg");
        medication.setStockMin(5);
        medication.setActive(true);
        return medication;
    }

    @Test
    @DisplayName("La existencia es la suma de los lotes vigentes")
    void existenciaSumaLotesVigentes() {
        Batch a = new Batch();
        a.setQuantity(3);
        Batch b = new Batch();
        b.setQuantity(4);
        when(batchRepository.findByMedicationIdAndExpiresAtGreaterThanEqualAndQuantityGreaterThanOrderByExpiresAtAsc(
                eq("m1"), any(), eq(0)))
                .thenReturn(List.of(a, b));

        assertThat(service.available("m1")).isEqualTo(7);
    }

    @Test
    @DisplayName("Recibir el mismo lote con otra fecha de vencimiento se rechaza")
    void loteConOtraFechaSeRechaza() {
        when(medicationRepository.findById("m1")).thenReturn(Optional.of(medication()));
        Batch existing = new Batch();
        existing.setBatchNumber("L-001");
        existing.setExpiresAt(LocalDate.now().plusMonths(3));
        existing.setQuantity(5);
        when(batchRepository.findByMedicationIdAndBatchNumber("m1", "L-001")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.receive(new PurchaseRequest("m1", "L-001",
                LocalDate.now().plusMonths(9), 4, "Proveedor"), "farmacia-1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya existe con vencimiento");
    }

    @Test
    @DisplayName("Recibir el mismo lote suma existencia en lugar de duplicarlo")
    void mismoLoteSumaExistencia() {
        when(medicationRepository.findById("m1")).thenReturn(Optional.of(medication()));
        Batch existing = new Batch();
        existing.setId("b1");
        existing.setBatchNumber("L-001");
        existing.setExpiresAt(LocalDate.now().plusMonths(3));
        existing.setQuantity(5);
        when(batchRepository.findByMedicationIdAndBatchNumber("m1", "L-001")).thenReturn(Optional.of(existing));
        when(batchRepository.save(any(Batch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.receive(new PurchaseRequest("m1", "L-001",
                LocalDate.now().plusMonths(3), 4, "Proveedor"), "farmacia-1");

        assertThat(response.quantity()).isEqualTo(9);
    }

    @Test
    @DisplayName("Un medicamento bajo el minimo genera alerta de stock")
    void alertaDeStockBajo() {
        when(medicationRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(medication()));
        when(batchRepository.findByMedicationIdAndExpiresAtGreaterThanEqualAndQuantityGreaterThanOrderByExpiresAtAsc(
                eq("m1"), any(), eq(0)))
                .thenReturn(List.of());
        when(batchRepository.findByQuantityGreaterThanAndExpiresAtLessThanEqualOrderByExpiresAtAsc(any(Integer.class), any()))
                .thenReturn(List.of());

        var alerts = service.alerts();

        assertThat(alerts).anyMatch(alert -> alert.type().equals("STOCK_BAJO"));
    }
}
