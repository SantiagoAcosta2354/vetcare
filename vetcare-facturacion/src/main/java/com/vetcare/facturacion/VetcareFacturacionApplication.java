package com.vetcare.facturacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class VetcareFacturacionApplication {

    public static void main(String[] args) {
        SpringApplication.run(VetcareFacturacionApplication.class, args);
    }
}
