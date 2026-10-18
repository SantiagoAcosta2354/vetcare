package com.vetcare.clinica.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.vetcare.clinica.client.PacientesClient;
import com.vetcare.clinica.domain.MedicalRecord;
import com.vetcare.clinica.domain.Species;
import com.vetcare.clinica.domain.Vaccination;
import com.vetcare.clinica.domain.Vaccine;
import com.vetcare.clinica.exception.BusinessRuleException;
import com.vetcare.clinica.exception.ResourceNotFoundException;
import com.vetcare.clinica.repository.VaccinationRepository;
import com.vetcare.clinica.repository.VaccineRepository;
import com.vetcare.clinica.web.dto.VaccinationRequest;
import com.vetcare.clinica.web.dto.VaccinationResponse;
import com.vetcare.clinica.web.dto.VaccineRequest;
import com.vetcare.clinica.web.dto.VaccineResponse;

/**
 * Vacunacion. El esquema depende de la especie: el sistema calcula la fecha de
 * la proxima dosis con los dias configurados en la vacuna y la deja visible para
 * el dueno.
 */
@Service
public class VaccinationService {

    private final VaccinationRepository vaccinationRepository;
    private final VaccineRepository vaccineRepository;
    private final MedicalRecordService medicalRecordService;
    private final PacientesClient pacientesClient;

    public VaccinationService(VaccinationRepository vaccinationRepository, VaccineRepository vaccineRepository,
            MedicalRecordService medicalRecordService, PacientesClient pacientesClient) {
        this.vaccinationRepository = vaccinationRepository;
        this.vaccineRepository = vaccineRepository;
        this.medicalRecordService = medicalRecordService;
        this.pacientesClient = pacientesClient;
    }

    // ------------------------------------------------------------- catalogo

    public List<VaccineResponse> vaccines() {
        return vaccineRepository.findByActiveTrueOrderByNameAsc().stream().map(VaccineResponse::from).toList();
    }

    public VaccineResponse createVaccine(VaccineRequest request) {
        Vaccine vaccine = new Vaccine();
        vaccine.setName(request.name().trim());
        vaccine.setSpecies(request.species() == null ? Species.AMBAS : request.species());
        vaccine.setNextDoseDays(request.nextDoseDays());
        vaccine.setActive(true);
        return VaccineResponse.from(vaccineRepository.save(vaccine));
    }

    public Vaccine requireVaccine(String id) {
        return vaccineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una vacuna con el id " + id));
    }

    // ----------------------------------------------------------- aplicacion

    public VaccinationResponse apply(String recordId, VaccinationRequest request, String actor) {
        MedicalRecord record = medicalRecordService.requireOpen(recordId);
        Vaccine vaccine = requireVaccine(request.vaccineId());

        if (vaccine.getSpecies() != Species.AMBAS && record.getPetSpecies() != null
                && vaccine.getSpecies() != record.getPetSpecies()) {
            throw new BusinessRuleException("La vacuna " + vaccine.getName() + " no corresponde a la especie del paciente");
        }

        Vaccination vaccination = new Vaccination();
        vaccination.setRecordId(record.getId());
        vaccination.setPetId(record.getPetId());
        vaccination.setPetName(record.getPetName());
        vaccination.setOwnerId(record.getOwnerId());
        vaccination.setVaccineId(vaccine.getId());
        vaccination.setVaccineName(vaccine.getName());
        vaccination.setBatch(request.batch());
        vaccination.setAppliedAt(request.appliedAt());
        vaccination.setNextDoseAt(request.appliedAt().plusDays(vaccine.getNextDoseDays()));
        vaccination.setAppliedBy(actor);
        vaccination.setCreatedAt(Instant.now());
        return VaccinationResponse.from(vaccinationRepository.save(vaccination));
    }

    public List<VaccinationResponse> findByPet(String petId) {
        return vaccinationRepository.findByPetIdOrderByAppliedAtDesc(petId).stream()
                .map(VaccinationResponse::from).toList();
    }

    public List<VaccinationResponse> upcoming(int days) {
        LocalDate today = LocalDate.now();
        return vaccinationRepository.findByNextDoseAtBetweenOrderByNextDoseAtAsc(today, today.plusDays(days)).stream()
                .map(VaccinationResponse::from).toList();
    }

    /**
     * Proximas dosis para el portal. La pertenencia se verifica contra el
     * servicio de pacientes con el token del dueno, no con un identificador que
     * llegue por la URL.
     */
    public List<VaccinationResponse> forOwnerPet(String petId) {
        pacientesClient.petFromPortal(petId);
        return findByPet(petId);
    }
}
