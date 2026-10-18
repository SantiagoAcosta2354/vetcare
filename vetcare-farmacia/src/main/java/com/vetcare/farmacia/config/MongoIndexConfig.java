package com.vetcare.farmacia.config;

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

import com.vetcare.farmacia.domain.Batch;
import com.vetcare.farmacia.domain.Dispensing;
import com.vetcare.farmacia.domain.Medication;

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
            mongoTemplate.indexOps(Medication.class).ensureIndex(
                    new Index().on("name", Sort.Direction.ASC).named("ix_medications_name"));

            // Un lote se identifica por medicamento + numero de lote: recibir dos
            // veces el mismo lote suma existencia, no crea un duplicado.
            mongoTemplate.indexOps(Batch.class).ensureIndex(new CompoundIndexDefinition(
                    new Document("medicationId", 1).append("batchNumber", 1))
                    .unique().named("uk_batches_medication_batch"));
            mongoTemplate.indexOps(Batch.class).ensureIndex(
                    new Index().on("expiresAt", Sort.Direction.ASC).named("ix_batches_expires"));

            mongoTemplate.indexOps(Dispensing.class).ensureIndex(
                    new Index().on("prescriptionId", Sort.Direction.ASC).unique().named("uk_dispensings_prescription"));
            mongoTemplate.indexOps(Dispensing.class).ensureIndex(
                    new Index().on("status", Sort.Direction.ASC).named("ix_dispensings_status"));
            mongoTemplate.indexOps(Dispensing.class).ensureIndex(
                    new Index().on("petId", Sort.Direction.ASC).named("ix_dispensings_pet"));
            log.info("Indices de MongoDB verificados en vetcare_farmacia");
        };
    }
}
