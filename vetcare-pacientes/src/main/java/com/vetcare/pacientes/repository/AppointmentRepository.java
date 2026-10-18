package com.vetcare.pacientes.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vetcare.pacientes.domain.Appointment;
import com.vetcare.pacientes.domain.AppointmentStatus;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {

    List<Appointment> findByDateOrderByStartTimeAsc(LocalDate date);

    List<Appointment> findByDateAndClinicIdOrderByStartTimeAsc(LocalDate date, String clinicId);

    List<Appointment> findByDateAndProfessionalIdOrderByStartTimeAsc(LocalDate date, String professionalId);

    List<Appointment> findByDateAndProfessionalIdAndStatusIn(LocalDate date, String professionalId,
            Collection<AppointmentStatus> statuses);

    List<Appointment> findByPetIdOrderByDateDescStartTimeDesc(String petId);

    List<Appointment> findByOwnerIdOrderByDateDescStartTimeDesc(String ownerId);

    List<Appointment> findByDateBetweenOrderByDateAscStartTimeAsc(LocalDate from, LocalDate to);
}
