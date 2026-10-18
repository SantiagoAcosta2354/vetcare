package com.vetcare.clinica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class VetcareClinicaApplication {

    public static void main(String[] args) {
        SpringApplication.run(VetcareClinicaApplication.class, args);
    }
}
