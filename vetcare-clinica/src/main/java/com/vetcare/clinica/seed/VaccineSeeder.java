package com.vetcare.clinica.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.vetcare.clinica.domain.Species;
import com.vetcare.clinica.domain.Vaccine;
import com.vetcare.clinica.repository.VaccineRepository;

/** Catalogo de vacunas con su esquema de refuerzo por especie. */
@Configuration
@ConditionalOnProperty(name = "vetcare.seed.enabled", havingValue = "true", matchIfMissing = true)
public class VaccineSeeder {

    private static final Logger log = LoggerFactory.getLogger(VaccineSeeder.class);

    private final VaccineRepository vaccineRepository;

    public VaccineSeeder(VaccineRepository vaccineRepository) {
        this.vaccineRepository = vaccineRepository;
    }

    @Bean
    @Order(3)
    ApplicationRunner seedVaccines() {
        return args -> {
            seed("Rabia canina", Species.CANINO, 365);
            seed("Polivalente canina", Species.CANINO, 365);
            seed("Triple felina", Species.FELINO, 365);
            seed("Leucemia felina", Species.FELINO, 365);
            seed("Rabia felina", Species.FELINO, 365);
            log.info("Catalogo de vacunas verificado");
        };
    }

    private void seed(String name, Species species, int nextDoseDays) {
        boolean exists = vaccineRepository.findByActiveTrueOrderByNameAsc().stream()
                .anyMatch(vaccine -> vaccine.getName().equalsIgnoreCase(name));
        if (exists) {
            return;
        }
        Vaccine vaccine = new Vaccine();
        vaccine.setName(name);
        vaccine.setSpecies(species);
        vaccine.setNextDoseDays(nextDoseDays);
        vaccine.setActive(true);
        vaccineRepository.save(vaccine);
    }
}
