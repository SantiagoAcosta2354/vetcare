package com.vetcare.clinica.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vetcare.clinica.client.PacientesClient;
import com.vetcare.clinica.domain.MedicalRecord;
import com.vetcare.clinica.domain.Species;
import com.vetcare.clinica.domain.Vaccination;
import com.vetcare.clinica.domain.Vaccine;
import com.vetcare.clinica.exception.BusinessRuleException;
import com.vetcare.clinica.repository.VaccinationRepository;
import com.vetcare.clinica.repository.VaccineRepository;
import com.vetcare.clinica.web.dto.VaccinationRequest;
import com.vetcare.clinica.web.dto.VaccinationResponse;

@ExtendWith(MockitoExtension.class)
class VaccinationServiceTest {

    @Mock
    private VaccinationRepository vaccinationRepository;
    @Mock
    private VaccineRepository vaccineRepository;
    @Mock
    private MedicalRecordService medicalRecordService;
    @Mock
    private PacientesClient pacientesClient;

    private VaccinationService service;

    @BeforeEach
    void setUp() {
        service = new VaccinationService(vaccinationRepository, vaccineRepository, medicalRecordService,
                pacientesClient);
    }

    private MedicalRecord record(Species species) {
        MedicalRecord record = new MedicalRecord();
        record.setId("r1");
        record.setPetId("p1");
        record.setPetName("Luna");
        record.setOwnerId("o1");
        record.setPetSpecies(species);
        record.setOpenedAt(Instant.now());
        return record;
    }

    private Vaccine vaccine(Species species, int days) {
        Vaccine vaccine = new Vaccine();
        vaccine.setId("vac1");
        vaccine.setName("Rabia");
        vaccine.setSpecies(species);
        vaccine.setNextDoseDays(days);
        vaccine.setActive(true);
        return vaccine;
    }

    @Test
    @DisplayName("La proxima dosis se calcula con los dias del esquema")
    void calculaProximaDosis() {
        when(medicalRecordService.requireOpen("r1")).thenReturn(record(Species.CANINO));
        when(vaccineRepository.findById("vac1")).thenReturn(java.util.Optional.of(vaccine(Species.CANINO, 365)));
        when(vaccinationRepository.save(any(Vaccination.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDate hoy = LocalDate.now();
        VaccinationResponse response = service.apply("r1", new VaccinationRequest("vac1", "L-9", hoy), "v1");

        assertThat(response.nextDoseAt()).isEqualTo(hoy.plusDays(365));
        assertThat(response.vaccineName()).isEqualTo("Rabia");
    }

    @Test
    @DisplayName("Una vacuna de otra especie no se aplica")
    void especieIncompatibleSeRechaza() {
        when(medicalRecordService.requireOpen("r1")).thenReturn(record(Species.FELINO));
        when(vaccineRepository.findById("vac1")).thenReturn(java.util.Optional.of(vaccine(Species.CANINO, 365)));

        assertThatThrownBy(() -> service.apply("r1", new VaccinationRequest("vac1", null, LocalDate.now()), "v1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("especie");

        verify(vaccinationRepository, never()).save(any(Vaccination.class));
    }

    @Test
    @DisplayName("Una vacuna marcada para ambas especies se aplica sin problema")
    void vacunaParaAmbasEspecies() {
        when(medicalRecordService.requireOpen("r1")).thenReturn(record(Species.FELINO));
        when(vaccineRepository.findById("vac1")).thenReturn(java.util.Optional.of(vaccine(Species.AMBAS, 30)));
        when(vaccinationRepository.save(any(Vaccination.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VaccinationResponse response = service.apply("r1", new VaccinationRequest("vac1", null, LocalDate.now()), "v1");

        assertThat(response.nextDoseAt()).isEqualTo(LocalDate.now().plusDays(30));
    }
}
