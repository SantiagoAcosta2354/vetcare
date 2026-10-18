package com.vetcare.farmacia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vetcare.farmacia.client.ClinicaClient;
import com.vetcare.farmacia.domain.Batch;
import com.vetcare.farmacia.domain.Dispensing;
import com.vetcare.farmacia.domain.DispensingStatus;
import com.vetcare.farmacia.domain.Medication;
import com.vetcare.farmacia.exception.BusinessRuleException;
import com.vetcare.farmacia.repository.BatchRepository;
import com.vetcare.farmacia.repository.DispensingRepository;
import com.vetcare.farmacia.web.dto.DispensingRequest;
import com.vetcare.farmacia.web.dto.DispensingResponse;

@ExtendWith(MockitoExtension.class)
class DispensingServiceTest {

    @Mock
    private DispensingRepository dispensingRepository;
    @Mock
    private BatchRepository batchRepository;
    @Mock
    private MedicationService medicationService;
    @Mock
    private ClinicaClient clinicaClient;

    private DispensingService service;

    @BeforeEach
    void setUp() {
        service = new DispensingService(dispensingRepository, batchRepository, medicationService, clinicaClient);
    }

    private Medication medication() {
        Medication medication = new Medication();
        medication.setId("m1");
        medication.setName("Amoxicilina 250 mg");
        medication.setActive(true);
        return medication;
    }

    private Batch batch(String id, String number, LocalDate expires, int quantity) {
        Batch batch = new Batch();
        batch.setId(id);
        batch.setMedicationId("m1");
        batch.setMedicationName("Amoxicilina 250 mg");
        batch.setBatchNumber(number);
        batch.setExpiresAt(expires);
        batch.setQuantity(quantity);
        return batch;
    }

    private DispensingRequest request(int quantity) {
        return new DispensingRequest("rx1", "rec1", "p1", "Luna", "o1",
                List.of(new DispensingRequest.Item("m1", quantity)), null);
    }

    @Test
    @DisplayName("Un despacho con existencia descuenta el lote y queda DESPACHADA")
    void despachoCompletoDescuenta() {
        when(dispensingRepository.findByPrescriptionId("rx1")).thenReturn(Optional.empty());
        when(medicationService.requireMedication("m1")).thenReturn(medication());
        when(batchRepository.findByMedicationIdAndExpiresAtGreaterThanEqualAndQuantityGreaterThanOrderByExpiresAtAsc(
                eq("m1"), any(), anyInt()))
                .thenReturn(List.of(batch("b1", "L-001", LocalDate.now().plusMonths(6), 10)));
        when(batchRepository.findById("b1")).thenReturn(Optional.of(batch("b1", "L-001", LocalDate.now().plusMonths(6), 10)));
        when(dispensingRepository.save(any(Dispensing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DispensingResponse response = service.dispense(request(4), "farmacia-1");

        assertThat(response.status()).isEqualTo(DispensingStatus.DESPACHADA);
        assertThat(response.items().get(0).getAllocations()).hasSize(1);
        assertThat(response.items().get(0).getAllocations().get(0).getQuantity()).isEqualTo(4);

        ArgumentCaptor<Batch> captor = ArgumentCaptor.forClass(Batch.class);
        verify(batchRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(6);
        verify(clinicaClient).notifyPrescription("rx1", DispensingStatus.DESPACHADA);
    }

    @Test
    @DisplayName("Sin existencia suficiente queda PENDIENTE_POR_STOCK y no descuenta nada")
    void sinStockQuedaPendiente() {
        when(dispensingRepository.findByPrescriptionId("rx1")).thenReturn(Optional.empty());
        when(medicationService.requireMedication("m1")).thenReturn(medication());
        when(batchRepository.findByMedicationIdAndExpiresAtGreaterThanEqualAndQuantityGreaterThanOrderByExpiresAtAsc(
                eq("m1"), any(), anyInt()))
                .thenReturn(List.of(batch("b1", "L-001", LocalDate.now().plusMonths(6), 2)));
        when(dispensingRepository.save(any(Dispensing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DispensingResponse response = service.dispense(request(5), "farmacia-1");

        assertThat(response.status()).isEqualTo(DispensingStatus.PENDIENTE_POR_STOCK);
        assertThat(response.dispensedAt()).isNull();
        verify(batchRepository, never()).save(any(Batch.class));
        verify(clinicaClient).notifyPrescription("rx1", DispensingStatus.PENDIENTE_POR_STOCK);
    }

    @Test
    @DisplayName("Un lote vencido no se entrega aunque el medicamento tenga existencia")
    void loteVencidoNoSeEntrega() {
        when(dispensingRepository.findByPrescriptionId("rx1")).thenReturn(Optional.empty());
        when(medicationService.requireMedication("m1")).thenReturn(medication());
        // El repositorio solo devuelve lotes vigentes: un vencido no aparece.
        when(batchRepository.findByMedicationIdAndExpiresAtGreaterThanEqualAndQuantityGreaterThanOrderByExpiresAtAsc(
                eq("m1"), any(), anyInt()))
                .thenReturn(List.of());
        when(dispensingRepository.save(any(Dispensing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DispensingResponse response = service.dispense(request(1), "farmacia-1");

        assertThat(response.status()).isEqualTo(DispensingStatus.PENDIENTE_POR_STOCK);
        verify(batchRepository, never()).save(any(Batch.class));
    }

    @Test
    @DisplayName("Se despacha primero el lote que vence antes")
    void despachaPrimeroElQueVenceAntes() {
        LocalDate hoy = LocalDate.now();
        Batch pronto = batch("b1", "L-001", hoy.plusMonths(1), 2);
        Batch despues = batch("b2", "L-002", hoy.plusMonths(8), 10);

        when(dispensingRepository.findByPrescriptionId("rx1")).thenReturn(Optional.empty());
        when(medicationService.requireMedication("m1")).thenReturn(medication());
        when(batchRepository.findByMedicationIdAndExpiresAtGreaterThanEqualAndQuantityGreaterThanOrderByExpiresAtAsc(
                eq("m1"), any(), anyInt()))
                .thenReturn(List.of(pronto, despues));
        when(batchRepository.findById("b1")).thenReturn(Optional.of(pronto));
        when(batchRepository.findById("b2")).thenReturn(Optional.of(despues));
        when(dispensingRepository.save(any(Dispensing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DispensingResponse response = service.dispense(request(5), "farmacia-1");

        assertThat(response.items().get(0).getAllocations()).hasSize(2);
        assertThat(response.items().get(0).getAllocations().get(0).getBatchNumber()).isEqualTo("L-001");
        assertThat(response.items().get(0).getAllocations().get(0).getQuantity()).isEqualTo(2);
        assertThat(pronto.getQuantity()).isZero();
        assertThat(despues.getQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("Una formula no se despacha dos veces")
    void noDespachaDosVeces() {
        Dispensing existing = new Dispensing();
        existing.setStatus(DispensingStatus.DESPACHADA);
        when(dispensingRepository.findByPrescriptionId("rx1")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.dispense(request(1), "farmacia-1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya tiene un despacho");

        verify(dispensingRepository, never()).save(any(Dispensing.class));
    }

    @Test
    @DisplayName("Reintentar un despacho ya completado se rechaza")
    void reintentoInvalidoSeRechaza() {
        Dispensing despachada = new Dispensing();
        despachada.setId("d1");
        despachada.setStatus(DispensingStatus.DESPACHADA);
        when(dispensingRepository.findById("d1")).thenReturn(Optional.of(despachada));

        assertThatThrownBy(() -> service.retry("d1", "farmacia-1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("pendiente por stock");
    }
}
