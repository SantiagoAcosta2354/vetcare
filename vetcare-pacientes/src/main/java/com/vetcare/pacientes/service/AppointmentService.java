package com.vetcare.pacientes.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.vetcare.pacientes.domain.Appointment;
import com.vetcare.pacientes.domain.AppointmentStatus;
import com.vetcare.pacientes.domain.Clinic;
import com.vetcare.pacientes.domain.Owner;
import com.vetcare.pacientes.domain.Pet;
import com.vetcare.pacientes.domain.PetStatus;
import com.vetcare.pacientes.exception.BusinessRuleException;
import com.vetcare.pacientes.exception.ResourceNotFoundException;
import com.vetcare.pacientes.repository.AppointmentRepository;
import com.vetcare.pacientes.web.dto.AppointmentRequest;
import com.vetcare.pacientes.web.dto.AppointmentResponse;
import com.vetcare.pacientes.web.dto.AppointmentSummary;
import com.vetcare.pacientes.web.dto.AvailabilityResponse;

/**
 * Agenda de la clinica.
 *
 * Reglas que se validan aqui y no en la pantalla:
 *   1. La franja pertenece al horario de la sede y empieza en un multiplo de la
 *      duracion configurada (30 minutos por defecto).
 *   2. Un profesional no puede tener dos citas que se solapen; la franja de una
 *      cita cancelada queda libre.
 *   3. Solo se agenda sobre pacientes activos.
 *   4. Las transiciones de estado son las del dominio: no se puede atender una
 *      cita cancelada ni volver a agendar una atendida.
 */
@Service
public class AppointmentService {

    private static final List<AppointmentStatus> OCCUPYING = List.of(
            AppointmentStatus.AGENDADA, AppointmentStatus.CONFIRMADA, AppointmentStatus.EN_ATENCION);

    private final AppointmentRepository appointmentRepository;
    private final PetService petService;
    private final ClinicService clinicService;
    private final OwnerService ownerService;

    public AppointmentService(AppointmentRepository appointmentRepository, PetService petService,
            ClinicService clinicService, OwnerService ownerService) {
        this.appointmentRepository = appointmentRepository;
        this.petService = petService;
        this.clinicService = clinicService;
        this.ownerService = ownerService;
    }

    // -------------------------------------------------------------- comandos

    public AppointmentResponse schedule(AppointmentRequest request, String actor) {
        Clinic clinic = clinicService.requireClinic(request.clinicId());
        if (!clinic.isActive()) {
            throw new BusinessRuleException("La sede esta inactiva; no admite citas nuevas");
        }
        Pet pet = petService.requirePet(request.petId());
        if (pet.getStatus() != PetStatus.ACTIVO) {
            throw new BusinessRuleException("El paciente " + pet.getName()
                    + " esta suspendido por solicitud del dueno; no se pueden agendar citas");
        }
        int slot = clinic.getSlotMinutes();
        LocalTime end = request.startTime().plusMinutes(slot);
        validateWithinOpeningHours(clinic, request.startTime(), end);
        validateSlotAlignment(clinic, request.startTime());

        // El solapamiento se comprueba antes de armar la cita: no tiene sentido
        // consultar al dueno para una franja que ya esta ocupada.
        if (overlaps(request.professionalId(), request.date(), request.startTime(), end, null)) {
            throw new BusinessRuleException("El profesional ya tiene una cita que se cruza con la franja "
                    + request.startTime() + " - " + end);
        }

        Owner owner = ownerService.requireOwner(pet.getOwnerId());

        Appointment appointment = new Appointment();
        appointment.setClinicId(clinic.getId());
        appointment.setClinicName(clinic.getName());
        appointment.setPetId(pet.getId());
        appointment.setPetName(pet.getName());
        appointment.setPetSpecies(pet.getSpecies());
        appointment.setOwnerId(owner.getId());
        appointment.setOwnerName(owner.getFullName());
        appointment.setProfessionalId(request.professionalId());
        appointment.setProfessionalName(request.professionalName());
        appointment.setDate(request.date());
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(end);
        appointment.setReason(request.reason().trim());
        appointment.setNotes(request.notes());
        appointment.setStatus(AppointmentStatus.AGENDADA);
        appointment.setCreatedBy(actor);
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    public AppointmentResponse changeStatus(String id, AppointmentStatus target, String reason, String actor) {
        Appointment appointment = requireAppointment(id);
        AppointmentStatus current = appointment.getStatus();

        if (!current.allowedNext().contains(target)) {
            throw new BusinessRuleException("Una cita en estado " + current.getLabel()
                    + " no puede pasar a " + target.getLabel());
        }
        if (target == AppointmentStatus.CANCELADA && (reason == null || reason.isBlank())) {
            throw new BusinessRuleException("Debe indicar el motivo de la cancelacion");
        }

        appointment.setStatus(target);
        switch (target) {
            case CONFIRMADA -> appointment.setConfirmedAt(Instant.now());
            case ATENDIDA -> appointment.setAttendedAt(Instant.now());
            case CANCELADA -> appointment.setCancellationReason(reason.trim());
            default -> {
                // EN_ATENCION y NO_ASISTIO no requieren marca adicional
            }
        }
        appointment.setUpdatedAt(Instant.now());
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    // ------------------------------------------------------------- consultas

    public AppointmentResponse findById(String id) {
        return AppointmentResponse.from(requireAppointment(id));
    }

    public List<AppointmentResponse> findByDate(LocalDate date, String clinicId) {
        List<Appointment> found = (clinicId == null || clinicId.isBlank())
                ? appointmentRepository.findByDateOrderByStartTimeAsc(date)
                : appointmentRepository.findByDateAndClinicIdOrderByStartTimeAsc(date, clinicId);
        return found.stream().map(AppointmentResponse::from).toList();
    }

    public List<AppointmentResponse> findByProfessional(LocalDate date, String professionalId) {
        return appointmentRepository.findByDateAndProfessionalIdOrderByStartTimeAsc(date, professionalId)
                .stream().map(AppointmentResponse::from).toList();
    }

    public List<AppointmentResponse> findByPet(String petId) {
        return appointmentRepository.findByPetIdOrderByDateDescStartTimeDesc(petId)
                .stream().map(AppointmentResponse::from).toList();
    }

    public List<AppointmentResponse> findByOwner(String ownerId) {
        return appointmentRepository.findByOwnerIdOrderByDateDescStartTimeDesc(ownerId)
                .stream().map(AppointmentResponse::from).toList();
    }

    public List<AppointmentResponse> findBetween(LocalDate from, LocalDate to) {
        return appointmentRepository.findByDateBetweenOrderByDateAscStartTimeAsc(from, to)
                .stream().map(AppointmentResponse::from).toList();
    }

    /** Franjas del dia con la marca de disponibilidad, para que la vista no calcule solapamientos. */
    public AvailabilityResponse availability(String professionalId, String clinicId, LocalDate date) {
        Clinic clinic = clinicService.requireClinic(clinicId);
        List<Appointment> busy = appointmentRepository
                .findByDateAndProfessionalIdAndStatusIn(date, professionalId, OCCUPYING);

        List<AvailabilityResponse.Slot> slots = new ArrayList<>();
        LocalTime cursor = clinic.getOpeningTime();
        while (!cursor.plusMinutes(clinic.getSlotMinutes()).isAfter(clinic.getClosingTime())) {
            LocalTime end = cursor.plusMinutes(clinic.getSlotMinutes());
            slots.add(new AvailabilityResponse.Slot(cursor, end, !intersects(busy, cursor, end)));
            cursor = end;
        }
        return new AvailabilityResponse(date, professionalId, slots);
    }

    public AppointmentSummary summary(LocalDate date) {
        List<Appointment> day = appointmentRepository.findByDateOrderByStartTimeAsc(date);
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (AppointmentStatus status : AppointmentStatus.values()) {
            byStatus.put(status.name(), 0L);
        }
        for (Appointment appointment : day) {
            byStatus.merge(appointment.getStatus().name(), 1L, Long::sum);
        }
        long attended = byStatus.getOrDefault(AppointmentStatus.ATENDIDA.name(), 0L);
        long cancelled = byStatus.getOrDefault(AppointmentStatus.CANCELADA.name(), 0L)
                + byStatus.getOrDefault(AppointmentStatus.NO_ASISTIO.name(), 0L);
        long pending = day.size() - attended - cancelled;
        return new AppointmentSummary(date, day.size(), attended, pending, cancelled, byStatus);
    }

    public Appointment requireAppointment(String id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una cita con el id " + id));
    }

    // ---------------------------------------------------------------- apoyo

    private boolean overlaps(String professionalId, LocalDate date, LocalTime start, LocalTime end, String ignoreId) {
        return intersects(appointmentRepository
                .findByDateAndProfessionalIdAndStatusIn(date, professionalId, OCCUPYING), start, end, ignoreId);
    }

    private boolean intersects(List<Appointment> appointments, LocalTime start, LocalTime end) {
        return intersects(appointments, start, end, null);
    }

    private boolean intersects(List<Appointment> appointments, LocalTime start, LocalTime end, String ignoreId) {
        return appointments.stream()
                .filter(existing -> ignoreId == null || !existing.getId().equals(ignoreId))
                .anyMatch(existing -> existing.getStartTime().isBefore(end) && existing.getEndTime().isAfter(start));
    }

    private void validateWithinOpeningHours(Clinic clinic, LocalTime start, LocalTime end) {
        if (start.isBefore(clinic.getOpeningTime()) || end.isAfter(clinic.getClosingTime())) {
            throw new BusinessRuleException("La cita debe estar dentro del horario de la sede ("
                    + clinic.getOpeningTime() + " - " + clinic.getClosingTime() + ")");
        }
    }

    private void validateSlotAlignment(Clinic clinic, LocalTime start) {
        int minutes = start.getHour() * 60 + start.getMinute();
        if (minutes % clinic.getSlotMinutes() != 0) {
            throw new BusinessRuleException("Las citas empiezan en franjas de " + clinic.getSlotMinutes()
                    + " minutos; " + start + " no corresponde a una franja valida");
        }
    }
}
