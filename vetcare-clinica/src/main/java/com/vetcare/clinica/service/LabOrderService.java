package com.vetcare.clinica.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.vetcare.clinica.domain.LabOrder;
import com.vetcare.clinica.domain.LabStatus;
import com.vetcare.clinica.domain.MedicalRecord;
import com.vetcare.clinica.exception.BusinessRuleException;
import com.vetcare.clinica.exception.ResourceNotFoundException;
import com.vetcare.clinica.repository.LabOrderRepository;
import com.vetcare.clinica.web.dto.LabOrderRequest;
import com.vetcare.clinica.web.dto.LabOrderResponse;
import com.vetcare.clinica.web.dto.LabResultRequest;

/**
 * Examenes. La orden queda pendiente hasta que el laboratorio registra el
 * resultado; ese estado es el que impide cobrarla como entregada.
 */
@Service
public class LabOrderService {

    private final LabOrderRepository labOrderRepository;
    private final MedicalRecordService medicalRecordService;

    public LabOrderService(LabOrderRepository labOrderRepository, MedicalRecordService medicalRecordService) {
        this.labOrderRepository = labOrderRepository;
        this.medicalRecordService = medicalRecordService;
    }

    public LabOrderResponse order(String recordId, LabOrderRequest request, String actor) {
        MedicalRecord record = medicalRecordService.requireOpen(recordId);

        LabOrder order = new LabOrder();
        order.setRecordId(record.getId());
        order.setAppointmentId(record.getAppointmentId());
        order.setPetId(record.getPetId());
        order.setPetName(record.getPetName());
        order.setOwnerId(record.getOwnerId());
        order.setExamName(request.examName().trim());
        order.setNotes(request.notes());
        order.setStatus(LabStatus.PENDIENTE);
        order.setOrderedAt(Instant.now());
        order.setOrderedBy(actor);
        return LabOrderResponse.from(labOrderRepository.save(order));
    }

    public LabOrderResponse registerResult(String id, LabResultRequest request, String actor) {
        LabOrder order = requireOrder(id);
        if (order.getStatus() == LabStatus.ENTREGADO) {
            throw new BusinessRuleException("El examen ya tiene resultado registrado");
        }
        order.setResultValues(request.values().trim());
        order.setResultObservations(request.observations());
        order.setResultAt(Instant.now());
        order.setResultBy(actor);
        order.setStatus(LabStatus.ENTREGADO);
        return LabOrderResponse.from(labOrderRepository.save(order));
    }

    public LabOrderResponse findById(String id) {
        return LabOrderResponse.from(requireOrder(id));
    }

    public List<LabOrderResponse> findByStatus(LabStatus status) {
        return labOrderRepository.findByStatusOrderByOrderedAtAsc(status).stream()
                .map(LabOrderResponse::from).toList();
    }

    public List<LabOrderResponse> findByPet(String petId) {
        return labOrderRepository.findByPetIdOrderByOrderedAtDesc(petId).stream()
                .map(LabOrderResponse::from).toList();
    }

    public LabOrder requireOrder(String id) {
        return labOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una orden de examen con el id " + id));
    }
}
