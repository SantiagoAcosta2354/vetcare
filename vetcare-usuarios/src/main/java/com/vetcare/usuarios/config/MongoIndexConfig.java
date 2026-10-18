package com.vetcare.usuarios.config;

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

import com.vetcare.usuarios.domain.User;

/**
 * El indice unico sobre el correo es la garantia a nivel de base de que no
 * existan dos cuentas con el mismo correo, incluso si dos peticiones llegan al
 * mismo tiempo. La validacion en el servicio da el mensaje claro; el indice
 * evita la duplicidad real.
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
            mongoTemplate.indexOps(User.class)
                    .ensureIndex(new Index().on("email", Sort.Direction.ASC).unique().named("uk_users_email"));
            log.info("Indices de MongoDB verificados en vetcare_usuarios");
        };
    }
}
