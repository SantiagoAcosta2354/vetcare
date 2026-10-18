package com.vetcare.pacientes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class VetcarePacientesApplication {

    public static void main(String[] args) {
        SpringApplication.run(VetcarePacientesApplication.class, args);
    }
}
