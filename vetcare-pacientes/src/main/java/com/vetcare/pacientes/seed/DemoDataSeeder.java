package com.vetcare.pacientes.seed;

import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.vetcare.pacientes.domain.Clinic;
import com.vetcare.pacientes.domain.DocumentType;
import com.vetcare.pacientes.domain.Owner;
import com.vetcare.pacientes.domain.Pet;
import com.vetcare.pacientes.domain.PetStatus;
import com.vetcare.pacientes.domain.Species;
import com.vetcare.pacientes.repository.ClinicRepository;
import com.vetcare.pacientes.repository.OwnerRepository;
import com.vetcare.pacientes.repository.PetRepository;

/**
 * Sede, dueno y paciente de demostracion. Sin esto la agenda y la facturacion
 * arrancan vacias y no hay forma de mostrar el flujo completo en la sustentacion.
 * Todo se comprueba antes de crear: reiniciar el servicio no duplica datos.
 */
@Configuration
@ConditionalOnProperty(name = "vetcare.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final ClinicRepository clinicRepository;
    private final OwnerRepository ownerRepository;
    private final PetRepository petRepository;

    public DemoDataSeeder(ClinicRepository clinicRepository, OwnerRepository ownerRepository,
            PetRepository petRepository) {
        this.clinicRepository = clinicRepository;
        this.ownerRepository = ownerRepository;
        this.petRepository = petRepository;
    }

    @Bean
    @Order(3)
    ApplicationRunner seedDemoData() {
        return args -> {
            if (clinicRepository.count() == 0) {
                Clinic clinic = new Clinic();
                clinic.setName("Sede Centro");
                clinic.setAddress("Calle 45 # 12-30, Bogota");
                clinic.setPhone("601 555 0101");
                clinic.setOpeningTime(LocalTime.of(8, 0));
                clinic.setClosingTime(LocalTime.of(18, 0));
                clinic.setSlotMinutes(30);
                clinic.setActive(true);
                clinicRepository.save(clinic);
                log.info("Sede de demostracion creada");
            }

            Owner owner = ownerRepository.findByDocument("1020304050").orElseGet(() -> {
                Owner created = new Owner();
                created.setDocumentType(DocumentType.CC);
                created.setDocument("1020304050");
                created.setFullName("Ana Perez");
                created.setPhone("300 555 2211");
                // Mismo correo que la cuenta de demostracion del portal: al entrar
                // como DUENO, la ficha se vincula sola.
                created.setEmail("dueno@vetcare.co");
                created.setAddress("Carrera 8 # 22-15");
                created.setCreatedBy("sistema");
                created.setActive(true);
                log.info("Dueno de demostracion creado");
                return ownerRepository.save(created);
            });

            if (petRepository.findByOwnerIdOrderByNameAsc(owner.getId()).isEmpty()) {
                Pet pet = new Pet();
                pet.setOwnerId(owner.getId());
                pet.setName("Luna");
                pet.setSpecies(Species.CANINO);
                pet.setBreed("Criolla");
                pet.setSex("HEMBRA");
                pet.setWeightKg(12.4);
                pet.setMicrochip("900123456789012");
                pet.setStatus(PetStatus.ACTIVO);
                pet.setCreatedBy("sistema");
                petRepository.save(pet);
                log.info("Paciente de demostracion creado");
            }
        };
    }
}
