package com.vetcare.usuarios.seed;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vetcare.usuarios.domain.Role;
import com.vetcare.usuarios.domain.User;
import com.vetcare.usuarios.repository.UserRepository;

/**
 * Crea las cuentas de demostracion, una por perfil, para poder probar los
 * permisos de extremo a extremo.
 *
 * La comprobacion es por correo y no por "la base esta vacia": asi, cuando se
 * agrega un perfil nuevo, su cuenta aparece en el siguiente arranque aunque las
 * demas ya existan.
 */
@Configuration
@ConditionalOnProperty(name = "vetcare.seed.enabled", havingValue = "true", matchIfMissing = true)
public class UserSeeder {

    private static final Logger log = LoggerFactory.getLogger(UserSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${vetcare.seed.admin-email}")
    private String adminEmail;

    @Value("${vetcare.seed.admin-password}")
    private String adminPassword;

    @Value("${vetcare.seed.demo-password}")
    private String demoPassword;

    public UserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    @Order(2)
    ApplicationRunner seedUsers() {
        return args -> {
            int created = 0;
            created += createIfMissing("Administrador VetCare", adminEmail, adminPassword, Set.of(Role.ADMIN));
            created += createIfMissing("Recepcion VetCare", "recepcion@vetcare.co", demoPassword, Set.of(Role.RECEPCION));
            created += createIfMissing("Dra. Laura Gomez", "veterinario@vetcare.co", demoPassword, Set.of(Role.VETERINARIO));
            created += createIfMissing("Auxiliar de laboratorio", "laboratorio@vetcare.co", demoPassword,
                    Set.of(Role.LABORATORIO));
            created += createIfMissing("Farmacia VetCare", "farmacia@vetcare.co", demoPassword, Set.of(Role.FARMACIA));
            created += createIfMissing("Dueno de prueba", "dueno@vetcare.co", demoPassword, Set.of(Role.DUENO));
            if (created == 0) {
                log.info("Las cuentas de demostracion ya existen; no se creo ninguna");
            } else {
                log.info("Cuentas de demostracion creadas en este arranque: {}", created);
            }
        };
    }

    /** @return 1 si la cuenta se creo, 0 si ya existia. */
    private int createIfMissing(String fullName, String email, String rawPassword, Set<Role> roles) {
        String normalized = email.toLowerCase();
        if (userRepository.existsByEmail(normalized)) {
            return 0;
        }
        userRepository.save(User.create(fullName, normalized, null, passwordEncoder.encode(rawPassword), roles, "sistema"));
        return 1;
    }
}
