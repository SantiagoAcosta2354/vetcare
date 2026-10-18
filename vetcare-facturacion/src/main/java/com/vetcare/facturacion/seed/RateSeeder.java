package com.vetcare.facturacion.seed;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.vetcare.facturacion.domain.ItemType;
import com.vetcare.facturacion.domain.Rate;
import com.vetcare.facturacion.repository.RateRepository;

/**
 * Tarifas iniciales de demostracion. Son parametros administrables: el
 * administrador las cambia sin tocar el codigo y la factura toma el valor
 * vigente al emitir.
 */
@Configuration
@ConditionalOnProperty(name = "vetcare.seed.enabled", havingValue = "true", matchIfMissing = true)
public class RateSeeder {

    private static final Logger log = LoggerFactory.getLogger(RateSeeder.class);

    private final RateRepository rateRepository;

    public RateSeeder(RateRepository rateRepository) {
        this.rateRepository = rateRepository;
    }

    @Bean
    @Order(2)
    ApplicationRunner seedRates() {
        return args -> {
            createIfMissing("CONSULTA", "Consulta veterinaria general", "80000");
            createIfMissing("VACUNACION", "Aplicacion de vacuna", "45000");
            createIfMissing("DESPARASITACION", "Desparasitacion interna", "35000");
            createIfMissing("EXAMEN_LAB", "Examen de laboratorio", "60000");
            createIfMissing("CIRUGIA_MENOR", "Procedimiento quirurgico menor", "250000");
            log.info("Tarifas de demostracion verificadas");
        };
    }

    private void createIfMissing(String code, String name, String price) {
        if (rateRepository.findByCode(code).isPresent()) {
            return;
        }
        Rate rate = new Rate();
        rate.setCode(code);
        rate.setName(name);
        rate.setType(ItemType.SERVICIO);
        rate.setPrice(new BigDecimal(price));
        rate.setTaxRate(BigDecimal.ZERO);
        rate.setActive(true);
        rateRepository.save(rate);
    }
}
