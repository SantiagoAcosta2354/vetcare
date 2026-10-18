package com.vetcare.pacientes.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import com.vetcare.pacientes.domain.Appointment;
import com.vetcare.pacientes.domain.Owner;
import com.vetcare.pacientes.domain.Pet;

/**
 * Indices que sostienen las reglas de este servicio: documento unico del dueno,
 * microchip unico del paciente (disperso, porque es opcional) y busqueda de la
 * agenda por profesional y fecha.
 */
@Configuration
@ConditionalOnProperty(name = "vetcare.indexes.enabled", havingValue = "true", matchIfMissing = true)
public class MongoIndexConfig {

    private static final Logger log = LoggerFactory.getLogger(MongoIndexConfig.class);

    private final MongoTemplate mongoTemplate;

    public MongoIndexConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Bean
    @Order(1)
    ApplicationRunner ensureIndexes() {
        return args -> {
            mongoTemplate.indexOps(Owner.class).ensureIndex(
                    new Index().on("document", Sort.Direction.ASC).unique().named("uk_owners_document"));
            mongoTemplate.indexOps(Pet.class).ensureIndex(
                    new Index().on("microchip", Sort.Direction.ASC).unique().sparse().named("uk_pets_microchip"));
            mongoTemplate.indexOps(Pet.class).ensureIndex(
                    new Index().on("ownerId", Sort.Direction.ASC).named("ix_pets_owner"));
            mongoTemplate.indexOps(Appointment.class).ensureIndex(
                    new Index().on("professionalId", Sort.Direction.ASC).on("date", Sort.Direction.ASC)
                            .named("ix_appointments_professional_date"));
            mongoTemplate.indexOps(Appointment.class).ensureIndex(
                    new Index().on("date", Sort.Direction.ASC).named("ix_appointments_date"));
            log.info("Indices de MongoDB verificados en vetcare_pacientes");
        };
    }
}
