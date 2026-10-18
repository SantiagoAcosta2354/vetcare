package com.vetcare.pacientes.service;

import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.vetcare.pacientes.domain.Clinic;
import com.vetcare.pacientes.exception.BusinessRuleException;
import com.vetcare.pacientes.exception.ResourceNotFoundException;
import com.vetcare.pacientes.repository.ClinicRepository;
import com.vetcare.pacientes.web.dto.ClinicRequest;
import com.vetcare.pacientes.web.dto.ClinicResponse;

@Service
public class ClinicService {

    private final ClinicRepository clinicRepository;

    public ClinicService(ClinicRepository clinicRepository) {
        this.clinicRepository = clinicRepository;
    }

    public List<ClinicResponse> findAll() {
        return clinicRepository.findByActiveTrueOrderByNameAsc().stream().map(ClinicResponse::from).toList();
    }

    public ClinicResponse findById(String id) {
        return ClinicResponse.from(requireClinic(id));
    }

    public ClinicResponse create(ClinicRequest request) {
        LocalTime opening = request.openingTime() == null ? LocalTime.of(8, 0) : request.openingTime();
        LocalTime closing = request.closingTime() == null ? LocalTime.of(18, 0) : request.closingTime();
        if (!closing.isAfter(opening)) {
            throw new BusinessRuleException("La hora de cierre debe ser posterior a la de apertura");
        }
        Clinic clinic = new Clinic();
        clinic.setName(request.name().trim());
        clinic.setAddress(request.address());
        clinic.setPhone(request.phone());
        clinic.setOpeningTime(opening);
        clinic.setClosingTime(closing);
        clinic.setSlotMinutes(request.slotMinutes() == null ? 30 : request.slotMinutes());
        clinic.setActive(true);
        return ClinicResponse.from(clinicRepository.save(clinic));
    }

    public Clinic requireClinic(String id) {
        return clinicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una sede con el id " + id));
    }
}
