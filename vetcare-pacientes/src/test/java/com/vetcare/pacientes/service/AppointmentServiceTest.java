package com.vetcare.pacientes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vetcare.pacientes.domain.Appointment;
import com.vetcare.pacientes.domain.AppointmentStatus;
import com.vetcare.pacientes.domain.Clinic;
import com.vetcare.pacientes.domain.Owner;
import com.vetcare.pacientes.domain.Pet;
import com.vetcare.pacientes.domain.PetStatus;
import com.vetcare.pacientes.domain.Species;
import com.vetcare.pacientes.exception.BusinessRuleException;
import com.vetcare.pacientes.repository.AppointmentRepository;
import com.vetcare.pacientes.web.dto.AppointmentRequest;
import com.vetcare.pacientes.web.dto.AppointmentResponse;
import com.vetcare.pacientes.web.dto.AvailabilityResponse;

/**
 * Reglas de la agenda: solapamiento, franjas, horario, estado del paciente y
 * transiciones. Se simulan los repositorios: no hace falta MongoDB.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private PetService petService;
    @Mock
    private ClinicService clinicService;
    @Mock
    private OwnerService ownerService;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(appointmentRepository, petService, clinicService, ownerService);
    }

    private Clinic clinic() {
        Clinic clinic = new Clinic();
        clinic.setId("c1");
        clinic.setName("Sede Centro");
        clinic.setOpeningTime(LocalTime.of(8, 0));
        clinic.setClosingTime(LocalTime.of(18, 0));
        clinic.setSlotMinutes(30);
        clinic.setActive(true);
        return clinic;
    }

    private Pet pet(PetStatus status) {
        Pet pet = new Pet();
        pet.setId("p1");
        pet.setOwnerId("o1");
        pet.setName("Luna");
        pet.setSpecies(Species.CANINO);
        pet.setStatus(status);
        return pet;
    }

    private Owner owner() {
        Owner owner = new Owner();
        owner.setId("o1");
        owner.setFullName("Ana Perez");
        return owner;
    }

    private AppointmentRequest request(LocalTime start) {
        return new AppointmentRequest("c1", "p1", "v1", "Dra. Laura Gomez", TOMORROW, start,
                "Vacunacion", null);
    }

    private Appointment existing(String id, LocalTime start, AppointmentStatus status) {
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setStartTime(start);
        appointment.setEndTime(start.plusMinutes(30));
        appointment.setStatus(status);
        return appointment;
    }

    @Test
    @DisplayName("Agenda una cita en una franja libre del profesional")
    void agendaFranjaLibre() {
        when(clinicService.requireClinic("c1")).thenReturn(clinic());
        when(petService.requirePet("p1")).thenReturn(pet(PetStatus.ACTIVO));
        when(ownerService.requireOwner("o1")).thenReturn(owner());
        when(appointmentRepository.findByDateAndProfessionalIdAndStatusIn(eq(TOMORROW), eq("v1"), any()))
                .thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment saved = invocation.getArgument(0);
            saved.setId("a1");
            return saved;
        });

        AppointmentResponse response = appointmentService.schedule(request(LocalTime.of(9, 0)), "u-recepcion");

        assertThat(response.startTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(response.endTime()).isEqualTo(LocalTime.of(9, 30));
        assertThat(response.status()).isEqualTo(AppointmentStatus.AGENDADA);
        assertThat(response.ownerName()).isEqualTo("Ana Perez");
    }

    @Test
    @DisplayName("No agenda si el profesional ya tiene una cita que se cruza")
    void rechazaSolapamiento() {
        when(clinicService.requireClinic("c1")).thenReturn(clinic());
        when(petService.requirePet("p1")).thenReturn(pet(PetStatus.ACTIVO));
        when(appointmentRepository.findByDateAndProfessionalIdAndStatusIn(eq(TOMORROW), eq("v1"), any()))
                .thenReturn(List.of(existing("a0", LocalTime.of(9, 0), AppointmentStatus.AGENDADA)));

        // La cita existente cubre 09:00 - 09:30: pedir la misma franja se cruza.
        assertThatThrownBy(() -> appointmentService.schedule(request(LocalTime.of(9, 0)), "u-recepcion"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("se cruza");
    }

    @Test
    @DisplayName("Una cita cancelada libera la franja")
    void canceladaLiberaFranja() {
        when(clinicService.requireClinic("c1")).thenReturn(clinic());
        when(petService.requirePet("p1")).thenReturn(pet(PetStatus.ACTIVO));
        when(ownerService.requireOwner("o1")).thenReturn(owner());
        // El repositorio solo devuelve las citas que ocupan franja: la cancelada no aparece.
        when(appointmentRepository.findByDateAndProfessionalIdAndStatusIn(eq(TOMORROW), eq("v1"), any()))
                .thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponse response = appointmentService.schedule(request(LocalTime.of(9, 0)), "u-recepcion");

        assertThat(response.status()).isEqualTo(AppointmentStatus.AGENDADA);
    }

    @Test
    @DisplayName("No agenda fuera del horario de la sede")
    void rechazaFueraDeHorario() {
        when(clinicService.requireClinic("c1")).thenReturn(clinic());
        when(petService.requirePet("p1")).thenReturn(pet(PetStatus.ACTIVO));

        assertThatThrownBy(() -> appointmentService.schedule(request(LocalTime.of(17, 45)), "u-recepcion"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("horario de la sede");
    }

    @Test
    @DisplayName("No agenda en una franja que no es multiplo de 30 minutos")
    void rechazaFranjaNoAlineada() {
        when(clinicService.requireClinic("c1")).thenReturn(clinic());
        when(petService.requirePet("p1")).thenReturn(pet(PetStatus.ACTIVO));

        assertThatThrownBy(() -> appointmentService.schedule(request(LocalTime.of(9, 10)), "u-recepcion"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("franjas de 30 minutos");
    }

    @Test
    @DisplayName("No agenda sobre un paciente suspendido por el dueno")
    void rechazaPacienteSuspendido() {
        when(clinicService.requireClinic("c1")).thenReturn(clinic());
        when(petService.requirePet("p1")).thenReturn(pet(PetStatus.SUSPENDIDO));

        assertThatThrownBy(() -> appointmentService.schedule(request(LocalTime.of(10, 0)), "u-recepcion"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("suspendido");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("No se puede atender una cita cancelada")
    void transicionInvalidaSeRechaza() {
        Appointment cancelled = existing("a9", LocalTime.of(9, 0), AppointmentStatus.CANCELADA);
        when(appointmentRepository.findById("a9")).thenReturn(java.util.Optional.of(cancelled));

        assertThatThrownBy(() -> appointmentService.changeStatus("a9", AppointmentStatus.ATENDIDA, null, "u1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no puede pasar");
    }

    @Test
    @DisplayName("Cancelar exige motivo")
    void cancelarExigeMotivo() {
        Appointment scheduled = existing("a8", LocalTime.of(9, 0), AppointmentStatus.AGENDADA);
        when(appointmentRepository.findById("a8")).thenReturn(java.util.Optional.of(scheduled));

        assertThatThrownBy(() -> appointmentService.changeStatus("a8", AppointmentStatus.CANCELADA, "  ", "u1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("motivo");
    }

    @Test
    @DisplayName("Confirmar una cita agendada es una transicion permitida")
    void confirmarEsPermitido() {
        Appointment scheduled = existing("a7", LocalTime.of(9, 0), AppointmentStatus.AGENDADA);
        when(appointmentRepository.findById("a7")).thenReturn(java.util.Optional.of(scheduled));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponse response = appointmentService.changeStatus("a7", AppointmentStatus.CONFIRMADA, null, "u1");

        assertThat(response.status()).isEqualTo(AppointmentStatus.CONFIRMADA);
        assertThat(scheduled.getConfirmedAt()).isNotNull();
    }

    @Test
    @DisplayName("La disponibilidad marca ocupadas solo las franjas con cita vigente")
    void disponibilidadMarcaOcupadas() {
        when(clinicService.requireClinic("c1")).thenReturn(clinic());
        when(appointmentRepository.findByDateAndProfessionalIdAndStatusIn(eq(TOMORROW), eq("v1"), any()))
                .thenReturn(List.of(existing("a1", LocalTime.of(9, 0), AppointmentStatus.CONFIRMADA)));

        AvailabilityResponse response = appointmentService.availability("v1", "c1", TOMORROW);

        assertThat(response.slots()).hasSize(20);
        assertThat(response.slots()).filteredOn(slot -> slot.start().equals(LocalTime.of(9, 0)))
                .allMatch(slot -> !slot.available());
        assertThat(response.slots()).filteredOn(slot -> slot.start().equals(LocalTime.of(9, 30)))
                .allMatch(AvailabilityResponse.Slot::available);
    }
}
