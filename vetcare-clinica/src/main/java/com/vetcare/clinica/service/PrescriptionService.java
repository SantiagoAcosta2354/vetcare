package com.vetcare.clinica.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.vetcare.clinica.client.FarmaciaClient;
import com.vetcare.clinica.domain.MedicalRecord;
import com.vetcare.clinica.domain.Prescription;
import com.vetcare.clinica.domain.PrescriptionItem;
import com.vetcare.clinica.domain.PrescriptionStatus;
import com.vetcare.clinica.exception.BusinessRuleException;
import com.vetcare.clinica.exception.ResourceNotFoundException;
import com.vetcare.clinica.repository.PrescriptionRepository;
import com.vetcare.clinica.web.dto.PrescriptionRequest;
import com.vetcare.clinica.web.dto.PrescriptionResponse;

/**
 * Formulas.
 *
 * Se emiten solo dentro de una historia abierta y unicamente con medicamentos
 * que existen en el catalogo de farmacia. Formular no descuenta inventario: eso
 * ocurre al despachar, para que la historia no diga que se entrego algo que el
 * paciente nunca recibio.
 */
@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordService medicalRecordService;
    private final FarmaciaClient farmaciaClient;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
            MedicalRecordService medicalRecordService, FarmaciaClient farmaciaClient) {
        this.prescriptionRepository = prescriptionRepository;
        this.medicalRecordService = medicalRecordService;
        this.farmaciaClient = farmaciaClient;
    }

    public PrescriptionResponse issue(String recordId, PrescriptionRequest request, String actor) {
        MedicalRecord record = medicalRecordService.requireOpen(recordId);

        List<PrescriptionItem> items = new ArrayList<>();
        for (PrescriptionRequest.Item requested : request.items()) {
            FarmaciaClient.MedicationData medication = farmaciaClient.medication(requested.medicationId());
            items.add(new PrescriptionItem(medication.id(), medication.name(), requested.dose(),
                    requested.frequency(), requested.durationDays(), requested.quantity()));
        }

        Prescription prescription = new Prescription();
        prescription.setRecordId(record.getId());
        prescription.setAppointmentId(record.getAppointmentId());
        prescription.setPetId(record.getPetId());
        prescription.setPetName(record.getPetName());
        prescription.setOwnerId(record.getOwnerId());
        prescription.setProfessionalId(record.getProfessionalId());
        prescription.setItems(items);
        prescription.setObservations(request.observations());
        prescription.setStatus(PrescriptionStatus.EMITIDA);
        prescription.setIssuedAt(Instant.now());
        prescription.setIssuedBy(actor);
        return PrescriptionResponse.from(prescriptionRepository.save(prescription));
    }

    /**
     * Lo invoca farmacia al despachar. Solo se mueve entre EMITIDA,
     * PENDIENTE_POR_STOCK y DESPACHADA: una formula anulada no revive.
     */
    public PrescriptionResponse updateDispensingStatus(String id, PrescriptionStatus target) {
        Prescription prescription = requirePrescription(id);
        if (prescription.getStatus() == PrescriptionStatus.ANULADA) {
            throw new BusinessRuleException("La formula esta anulada y no cambia de estado");
        }
        if (target != PrescriptionStatus.DESPACHADA && target != PrescriptionStatus.PENDIENTE_POR_STOCK) {
            throw new BusinessRuleException("Farmacia solo puede marcar la formula como despachada o pendiente por stock");
        }
        if (prescription.getStatus() == PrescriptionStatus.DESPACHADA && target == PrescriptionStatus.PENDIENTE_POR_STOCK) {
            throw new BusinessRuleException("Una formula ya despachada no vuelve a pendiente");
        }
        prescription.setStatus(target);
        return PrescriptionResponse.from(prescriptionRepository.save(prescription));
    }

    public PrescriptionResponse findById(String id) {
        return PrescriptionResponse.from(requirePrescription(id));
    }

    public List<PrescriptionResponse> findByRecord(String recordId) {
        return prescriptionRepository.findByRecordIdOrderByIssuedAtDesc(recordId).stream()
                .map(PrescriptionResponse::from).toList();
    }

    public List<PrescriptionResponse> findByStatus(PrescriptionStatus status) {
        return prescriptionRepository.findByStatusOrderByIssuedAtAsc(status).stream()
                .map(PrescriptionResponse::from).toList();
    }

    public List<PrescriptionResponse> findByPet(String petId) {
        return prescriptionRepository.findByPetIdOrderByIssuedAtDesc(petId).stream()
                .map(PrescriptionResponse::from).toList();
    }

    public Prescription requirePrescription(String id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una formula con el id " + id));
    }
}
