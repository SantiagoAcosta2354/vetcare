package com.vetcare.clinica.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;

import com.vetcare.clinica.domain.LabOrder;
import com.vetcare.clinica.domain.MedicalRecord;
import com.vetcare.clinica.domain.Prescription;
import com.vetcare.clinica.domain.Vaccination;
import com.vetcare.clinica.domain.Vaccine;

import org.bson.Document;

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
            // Una sola historia por cita: la regla se sostiene en la base, no solo en el servicio.
            mongoTemplate.indexOps(MedicalRecord.class).ensureIndex(
                    new Index().on("appointmentId", Sort.Direction.ASC).unique().named("uk_records_appointment"));
            mongoTemplate.indexOps(MedicalRecord.class).ensureIndex(
                    new Index().on("petId", Sort.Direction.ASC).named("ix_records_pet"));

            mongoTemplate.indexOps(Prescription.class).ensureIndex(new CompoundIndexDefinition(
                    new Document("recordId", 1).append("status", 1)).named("ix_prescriptions_record_status"));
            mongoTemplate.indexOps(Prescription.class).ensureIndex(
                    new Index().on("petId", Sort.Direction.ASC).named("ix_prescriptions_pet"));

            mongoTemplate.indexOps(LabOrder.class).ensureIndex(
                    new Index().on("status", Sort.Direction.ASC).named("ix_laborders_status"));
            mongoTemplate.indexOps(LabOrder.class).ensureIndex(
                    new Index().on("petId", Sort.Direction.ASC).named("ix_laborders_pet"));

            mongoTemplate.indexOps(Vaccination.class).ensureIndex(
                    new Index().on("petId", Sort.Direction.ASC).named("ix_vaccinations_pet"));
            mongoTemplate.indexOps(Vaccination.class).ensureIndex(
                    new Index().on("nextDoseAt", Sort.Direction.ASC).named("ix_vaccinations_next_dose"));

            mongoTemplate.indexOps(Vaccine.class).ensureIndex(
                    new Index().on("name", Sort.Direction.ASC).unique().named("uk_vaccines_name"));
            log.info("Indices de MongoDB verificados en vetcare_clinica");
        };
    }
}
