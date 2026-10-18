package com.vetcare.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Capa de vista. No tiene base de datos propia: guarda el token en la sesion y
 * llama a los microservicios, que son los que aplican las reglas.
 */
@SpringBootApplication
public class VetcareWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(VetcareWebApplication.class, args);
    }
}
