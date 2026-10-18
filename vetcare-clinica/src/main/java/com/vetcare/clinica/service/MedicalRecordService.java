package com.vetcare.clinica.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.vetcare.clinica.client.PacientesClient;
import com.vetcare.clinica.domain.MedicalRecord;
import com.vetcare.clinica.domain.PrescriptionStatus;
import com.vetcare.clinica.domain.RecordStatus;
import com.vetcare.clinica.exception.BusinessRuleException;
import com.vetcare.clinica.exception.ResourceNotFoundException;
import com.vetcare.clinica.repository.MedicalRecordRepository;
import com.vetcare.clinica.repository.PrescriptionRepository;
import com.vetcare.clinica.web.dto.DiagnosisRequest;
import com.vetcare.clinica.web.dto.OpenRecordRequest;
import com.vetcare.clinica.web.dto.RecordResponse;
import com.vetcare.clinica.web.dto.VitalsRequest;

/**
 * Historia clinica.
 *
 * Reglas:
 *   1. Una sola historia por cita.
 *   2. Una historia cerrada no admite cambios: es el documento que respalda la
 *      atencion y la factura.
 *   3. Cerrar con formulas pendientes de despacho se permite, pero deja
 *      constancia escrita en la historia.
 */
@Service
public class MedicalRecordService {

    private static final List<PrescriptionStatus> PENDING_DISPATCH = List.of(
            PrescriptionStatus.EMITIDA, PrescriptionStatus.PENDIENTE_POR_STOCK);

    private final MedicalRecordRepository recordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PacientesClient pacientesClient;

    public MedicalRecordService(MedicalRecordRepository recordRepository,
            PrescriptionRepository prescriptionRepository, PacientesClient pacientesClient) {
        this.recordRepository = recordRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.pacientesClient = pacientesClient;
    }

    public RecordResponse open(OpenRecordRequest request, String actor) {
        if (recordRepository.existsByAppointmentId(request.appointmentId())) {
            throw new BusinessRuleException("La cita ya tiene una historia clinica abierta");
        }
        PacientesClient.AppointmentData appointment = pacientesClient.appointment(request.appointmentId());
        if (appointment == null) {
            throw new ResourceNotFoundException("No existe la cita indicada");
        }

        MedicalRecord record = new MedicalRecord();
        record.setAppointmentId(appointment.id());
        record.setPetId(appointment.petId());
        record.setPetName(appointment.petName());
        record.setPetSpecies(appointment.petSpecies());
        record.setOwnerId(appointment.ownerId());
        record.setOwnerName(appointment.ownerName());
        record.setProfessionalId(request.professionalId());
        record.setProfessionalName(request.professionalName());
        record.setReason(request.reason() == null || request.reason().isBlank()
                ? appointment.reason() : request.reason());
        record.setStatus(RecordStatus.ABIERTA);
        record.setOpenedAt(Instant.now());
        return RecordResponse.from(recordRepository.save(record));
    }

    public RecordResponse findById(String id) {
        return RecordResponse.from(requireRecord(id));
    }

    public RecordResponse findByAppointment(String appointmentId) {
        return RecordResponse.from(recordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("La cita no tiene historia clinica")));
    }

    public List<RecordResponse> findByPet(String petId) {
        return recordRepository.findByPetIdOrderByOpenedAtDesc(petId).stream().map(RecordResponse::from).toList();
    }

    public RecordResponse updateVitals(String id, VitalsRequest request) {
        MedicalRecord record = requireOpen(id);
        record.setWeightKg(request.weightKg());
        record.setTemperatureC(request.temperatureC());
        record.setHeartRate(request.heartRate());
        record.setRespiratoryRate(request.respiratoryRate());
        if (request.notes() != null && !request.notes().isBlank()) {
            record.setNotes(request.notes());
        }
        return RecordResponse.from(recordRepository.save(record));
    }

    public RecordResponse updateDiagnosis(String id, DiagnosisRequest request) {
        MedicalRecord record = requireOpen(id);
        record.setDiagnosis(request.diagnosis().trim());
        record.setTreatmentDescription(request.treatmentDescription().trim());
        record.setTreatmentDurationDays(request.treatmentDurationDays());
        record.setTreatmentIndications(request.treatmentIndications());
        if (request.notes() != null && !request.notes().isBlank()) {
            record.setNotes(request.notes());
        }
        return RecordResponse.from(recordRepository.save(record));
    }

    public RecordResponse close(String id, String note, String actor) {
        MedicalRecord record = requireRecord(id);
        if (record.getStatus() == RecordStatus.CERRADA) {
            throw new BusinessRuleException("La historia ya esta cerrada");
        }

        StringBuilder closure = new StringBuilder();
        if (prescriptionRepository.existsByRecordIdAndStatusIn(id, PENDING_DISPATCH)) {
            closure.append("Cerrada con formulas pendientes de despacho. ");
        }
        if (note != null && !note.isBlank()) {
            closure.append(note.trim());
        }
        record.setClosureNote(closure.length() == 0 ? null : closure.toString().trim());
        record.setStatus(RecordStatus.CERRADA);
        record.setClosedAt(Instant.now());
        record.setClosedBy(actor);
        return RecordResponse.from(recordRepository.save(record));
    }

    public MedicalRecord requireRecord(String id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una historia con el id " + id));
    }

    /** Devuelve la historia solo si sigue abierta; es el portero de toda escritura clinica. */
    public MedicalRecord requireOpen(String id) {
        MedicalRecord record = requireRecord(id);
        if (record.getStatus() == RecordStatus.CERRADA) {
            throw new BusinessRuleException("La historia esta cerrada y no admite cambios; "
                    + "corrija con una nota en una nueva atencion");
        }
        return record;
    }
}
