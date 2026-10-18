package com.vetcare.farmacia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class VetcareFarmaciaApplication {

    public static void main(String[] args) {
        SpringApplication.run(VetcareFarmaciaApplication.class, args);
    }
}
