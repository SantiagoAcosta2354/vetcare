package com.vetcare.clinica.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vetcare.clinica.client.PacientesClient;
import com.vetcare.clinica.domain.MedicalRecord;
import com.vetcare.clinica.domain.PrescriptionStatus;
import com.vetcare.clinica.domain.RecordStatus;
import com.vetcare.clinica.domain.Species;
import com.vetcare.clinica.exception.BusinessRuleException;
import com.vetcare.clinica.repository.MedicalRecordRepository;
import com.vetcare.clinica.repository.PrescriptionRepository;
import com.vetcare.clinica.web.dto.OpenRecordRequest;
import com.vetcare.clinica.web.dto.RecordResponse;
import com.vetcare.clinica.web.dto.VitalsRequest;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository recordRepository;
    @Mock
    private PrescriptionRepository prescriptionRepository;
    @Mock
    private PacientesClient pacientesClient;

    private MedicalRecordService service;

    @BeforeEach
    void setUp() {
        service = new MedicalRecordService(recordRepository, prescriptionRepository, pacientesClient);
    }

    private PacientesClient.AppointmentData appointment() {
        return new PacientesClient.AppointmentData("a1", "p1", "Luna", Species.CANINO, "o1", "Ana Perez",
                "EN_ATENCION", "Vacunacion");
    }

    private MedicalRecord record(RecordStatus status) {
        MedicalRecord record = new MedicalRecord();
        record.setId("r1");
        record.setAppointmentId("a1");
        record.setPetId("p1");
        record.setPetName("Luna");
        record.setPetSpecies(Species.CANINO);
        record.setStatus(status);
        record.setOpenedAt(Instant.now());
        return record;
    }

    @Test
    @DisplayName("La historia se abre con los datos de la cita y del paciente")
    void abreConDatosDeLaCita() {
        when(recordRepository.existsByAppointmentId("a1")).thenReturn(false);
        when(pacientesClient.appointment("a1")).thenReturn(appointment());
        when(recordRepository.save(any(MedicalRecord.class))).thenAnswer(invocation -> {
            MedicalRecord saved = invocation.getArgument(0);
            saved.setId("r1");
            return saved;
        });

        RecordResponse response = service.open(
                new OpenRecordRequest("a1", "v1", "Dra. Laura Gomez", null), "v1");

        assertThat(response.petName()).isEqualTo("Luna");
        assertThat(response.ownerName()).isEqualTo("Ana Perez");
        assertThat(response.petSpecies()).isEqualTo(Species.CANINO);
        assertThat(response.status()).isEqualTo(RecordStatus.ABIERTA);
        assertThat(response.reason()).isEqualTo("Vacunacion");
    }

    @Test
    @DisplayName("Una cita no puede tener dos historias")
    void noAbreDosHistorias() {
        when(recordRepository.existsByAppointmentId("a1")).thenReturn(true);

        assertThatThrownBy(() -> service.open(new OpenRecordRequest("a1", "v1", "Dra. Laura", null), "v1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya tiene una historia");

        verify(recordRepository, never()).save(any(MedicalRecord.class));
    }

    @Test
    @DisplayName("Una historia cerrada no admite cambios")
    void cerradaNoAdmiteCambios() {
        when(recordRepository.findById("r1")).thenReturn(Optional.of(record(RecordStatus.CERRADA)));

        assertThatThrownBy(() -> service.updateVitals("r1", new VitalsRequest(12.0, 38.5, 90, 24, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("cerrada");

        verify(recordRepository, never()).save(any(MedicalRecord.class));
    }

    @Test
    @DisplayName("Cerrar con formula pendiente deja constancia escrita")
    void cierreConFormulaPendienteDejaConstancia() {
        when(recordRepository.findById("r1")).thenReturn(Optional.of(record(RecordStatus.ABIERTA)));
        when(prescriptionRepository.existsByRecordIdAndStatusIn(eq("r1"), any())).thenReturn(true);
        when(recordRepository.save(any(MedicalRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecordResponse response = service.close("r1", "Control en 8 dias", "v1");

        assertThat(response.status()).isEqualTo(RecordStatus.CERRADA);
        assertThat(response.closureNote()).contains("pendientes de despacho").contains("Control en 8 dias");
        assertThat(response.closedBy()).isEqualTo("v1");
    }

    @Test
    @DisplayName("Cerrar sin formulas pendientes no agrega la constancia")
    void cierreSinPendientesNoAgregaConstancia() {
        when(recordRepository.findById("r1")).thenReturn(Optional.of(record(RecordStatus.ABIERTA)));
        when(prescriptionRepository.existsByRecordIdAndStatusIn(eq("r1"), any())).thenReturn(false);
        when(recordRepository.save(any(MedicalRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecordResponse response = service.close("r1", null, "v1");

        assertThat(response.closureNote()).isNull();
    }

    @Test
    @DisplayName("El estado de la formula pendiente se consulta con los estados correctos")
    void consultaEstadosPendientes() {
        when(recordRepository.findById("r1")).thenReturn(Optional.of(record(RecordStatus.ABIERTA)));
        when(prescriptionRepository.existsByRecordIdAndStatusIn(eq("r1"),
                eq(List.of(PrescriptionStatus.EMITIDA, PrescriptionStatus.PENDIENTE_POR_STOCK)))).thenReturn(true);
        when(recordRepository.save(any(MedicalRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.close("r1", null, "v1");

        verify(prescriptionRepository).existsByRecordIdAndStatusIn(eq("r1"),
                eq(List.of(PrescriptionStatus.EMITIDA, PrescriptionStatus.PENDIENTE_POR_STOCK)));
        verify(recordRepository, never()).deleteById(anyString());
    }
}
