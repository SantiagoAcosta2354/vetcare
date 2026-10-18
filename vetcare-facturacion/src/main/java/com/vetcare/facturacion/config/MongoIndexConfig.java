package com.vetcare.facturacion.config;

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

import com.vetcare.facturacion.domain.Invoice;
import com.vetcare.facturacion.domain.Payment;
import com.vetcare.facturacion.domain.Rate;

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
            mongoTemplate.indexOps(Rate.class).ensureIndex(
                    new Index().on("code", Sort.Direction.ASC).unique().named("uk_rates_code"));

            mongoTemplate.indexOps(Invoice.class).ensureIndex(
                    new Index().on("number", Sort.Direction.ASC).unique().named("uk_invoices_number"));
            mongoTemplate.indexOps(Invoice.class).ensureIndex(
                    new Index().on("petId", Sort.Direction.ASC).on("consultationDate", Sort.Direction.ASC)
                            .named("ix_invoices_pet_date"));
            mongoTemplate.indexOps(Invoice.class).ensureIndex(
                    new Index().on("ownerId", Sort.Direction.ASC).named("ix_invoices_owner"));
            mongoTemplate.indexOps(Invoice.class).ensureIndex(
                    new Index().on("status", Sort.Direction.ASC).named("ix_invoices_status"));

            mongoTemplate.indexOps(Payment.class).ensureIndex(
                    new Index().on("invoiceId", Sort.Direction.ASC).named("ix_payments_invoice"));
            log.info("Indices de MongoDB verificados en vetcare_facturacion");
        };
    }
}
