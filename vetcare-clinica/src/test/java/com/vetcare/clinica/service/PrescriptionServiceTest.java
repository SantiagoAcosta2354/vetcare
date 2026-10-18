package com.vetcare.clinica.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import com.vetcare.clinica.client.FarmaciaClient;
import com.vetcare.clinica.domain.MedicalRecord;
import com.vetcare.clinica.domain.Prescription;
import com.vetcare.clinica.domain.PrescriptionStatus;
import com.vetcare.clinica.domain.RecordStatus;
import com.vetcare.clinica.exception.BusinessRuleException;
import com.vetcare.clinica.repository.PrescriptionRepository;
import com.vetcare.clinica.web.dto.PrescriptionRequest;
import com.vetcare.clinica.web.dto.PrescriptionResponse;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;
    @Mock
    private MedicalRecordService medicalRecordService;
    @Mock
    private FarmaciaClient farmaciaClient;

    private PrescriptionService service;

    @BeforeEach
    void setUp() {
        service = new PrescriptionService(prescriptionRepository, medicalRecordService, farmaciaClient);
    }

    private MedicalRecord record() {
        MedicalRecord record = new MedicalRecord();
        record.setId("r1");
        record.setAppointmentId("a1");
        record.setPetId("p1");
        record.setPetName("Luna");
        record.setOwnerId("o1");
        record.setProfessionalId("v1");
        record.setStatus(RecordStatus.ABIERTA);
        record.setOpenedAt(Instant.now());
        return record;
    }

    private PrescriptionRequest request() {
        return new PrescriptionRequest(
                List.of(new PrescriptionRequest.Item("m1", "1 ml cada 12 h", "Cada 12 horas", 7, 14)), null);
    }

    @Test
    @DisplayName("La formula toma el nombre del medicamento del catalogo de farmacia")
    void tomaNombreDelCatalogo() {
        when(medicalRecordService.requireOpen("r1")).thenReturn(record());
        when(farmaciaClient.medication("m1"))
                .thenReturn(new FarmaciaClient.MedicationData("m1", "Amoxicilina 250 mg", "Tableta", true));
        when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(invocation -> {
            Prescription saved = invocation.getArgument(0);
            saved.setId("rx1");
            return saved;
        });

        PrescriptionResponse response = service.issue("r1", request(), "v1");

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).getMedicationName()).isEqualTo("Amoxicilina 250 mg");
        assertThat(response.status()).isEqualTo(PrescriptionStatus.EMITIDA);
    }

    @Test
    @DisplayName("No se formula sobre una historia cerrada")
    void noFormulaSobreHistoriaCerrada() {
        when(medicalRecordService.requireOpen("r1"))
                .thenThrow(new BusinessRuleException("La historia esta cerrada y no admite cambios"));

        assertThatThrownBy(() -> service.issue("r1", request(), "v1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("cerrada");

        verify(prescriptionRepository, never()).save(any(Prescription.class));
    }

    @Test
    @DisplayName("Si el medicamento no existe en farmacia, la formula no se emite")
    void medicamentoInexistenteNoSeFormula() {
        when(medicalRecordService.requireOpen("r1")).thenReturn(record());
        when(farmaciaClient.medication("m1"))
                .thenThrow(new BusinessRuleException("El medicamento indicado no existe en el catalogo de farmacia"));

        assertThatThrownBy(() -> service.issue("r1", request(), "v1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("catalogo");

        verify(prescriptionRepository, never()).save(any(Prescription.class));
    }

    @Test
    @DisplayName("Farmacia no puede anular una formula")
    void farmaciaNoAnula() {
        Prescription prescription = new Prescription();
        prescription.setId("rx1");
        prescription.setStatus(PrescriptionStatus.EMITIDA);
        when(prescriptionRepository.findById("rx1")).thenReturn(Optional.of(prescription));

        assertThatThrownBy(() -> service.updateDispensingStatus("rx1", PrescriptionStatus.ANULADA))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("despachada o pendiente");
    }

    @Test
    @DisplayName("Una formula despachada no vuelve a pendiente por stock")
    void despachadaNoVuelveAPendiente() {
        Prescription prescription = new Prescription();
        prescription.setId("rx1");
        prescription.setStatus(PrescriptionStatus.DESPACHADA);
        when(prescriptionRepository.findById("rx1")).thenReturn(Optional.of(prescription));

        assertThatThrownBy(() -> service.updateDispensingStatus("rx1", PrescriptionStatus.PENDIENTE_POR_STOCK))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no vuelve a pendiente");
    }
}
