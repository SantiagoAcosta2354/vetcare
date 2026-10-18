package com.vetcare.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Punto de entrada del microservicio de cuentas.
 *
 * Es el unico servicio de la plataforma que firma tokens: los demas validan la
 * firma con el secreto compartido, de modo que una caida de este servicio no
 * bloquea la operacion clinica ni la facturacion.
 */
@SpringBootApplication
@EnableMongoAuditing
public class VetcareUsuariosApplication {

    public static void main(String[] args) {
        SpringApplication.run(VetcareUsuariosApplication.class, args);
    }
}
