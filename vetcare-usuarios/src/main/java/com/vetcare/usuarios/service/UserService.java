package com.vetcare.usuarios.service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vetcare.usuarios.domain.Role;
import com.vetcare.usuarios.domain.User;
import com.vetcare.usuarios.exception.BusinessRuleException;
import com.vetcare.usuarios.exception.InvalidCredentialsException;
import com.vetcare.usuarios.exception.ResourceNotFoundException;
import com.vetcare.usuarios.repository.UserRepository;
import com.vetcare.usuarios.web.dto.ChangePasswordRequest;
import com.vetcare.usuarios.web.dto.CreateUserRequest;
import com.vetcare.usuarios.web.dto.RegisterRequest;
import com.vetcare.usuarios.web.dto.UpdateUserRequest;
import com.vetcare.usuarios.web.dto.UserResponse;

/**
 * Reglas de cuentas y roles.
 *
 * Dos reglas sostienen la administracion del sistema:
 *   1. Nadie puede quedarse sin el rol ADMIN: ni retirarselo a si mismo ni
 *      dejar la plataforma sin administradores activos.
 *   2. Todo cambio de rol o de estado guarda autor y fecha, porque el token
 *      actual no dice quien hizo el cambio cuando lo hizo otro administrador.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ------------------------------------------------------------- consultas

    public List<UserResponse> findAll(boolean includeInactive) {
        List<User> users = includeInactive
                ? userRepository.findAll()
                : userRepository.findByActiveTrueOrderByFullNameAsc();
        return users.stream().map(UserResponse::from).toList();
    }

    public UserResponse findById(String id) {
        return UserResponse.from(requireUser(id));
    }

    /** Personal activo con un rol concreto; lo usa la agenda para listar veterinarios. */
    public List<UserResponse> findByRole(Role role) {
        return userRepository.findByRolesContaining(role).stream()
                .filter(User::isActive)
                .map(UserResponse::from)
                .toList();
    }

    // -------------------------------------------------------------- comandos

    /** Registro publico: siempre crea un DUENO. */
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessRuleException("Ya existe una cuenta registrada con el correo " + email);
        }
        User user = User.create(request.fullName().trim(), email, trimToNull(request.phone()),
                passwordEncoder.encode(request.password()), Set.of(Role.DUENO), "autoregistro");
        return UserResponse.from(userRepository.save(user));
    }

    /** Creacion desde la administracion; permite asignar cualquier perfil. */
    public UserResponse create(CreateUserRequest request, String actor) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessRuleException("Ya existe una cuenta registrada con el correo " + email);
        }
        User user = User.create(request.fullName().trim(), email, trimToNull(request.phone()),
                passwordEncoder.encode(request.password()), request.roles(), actor);
        return UserResponse.from(userRepository.save(user));
    }

    /**
     * Verificacion de credenciales. Se lanza el mismo error tanto si el correo no
     * existe como si la contrasena es incorrecta, para no revelar que cuentas
     * estan registradas.
     */
    public User authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new InvalidCredentialsException("Correo o contrasena incorrectos"));
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Correo o contrasena incorrectos");
        }
        if (!user.canAuthenticate()) {
            throw new InvalidCredentialsException("Correo o contrasena incorrectos");
        }
        user.setLastLoginAt(Instant.now());
        return userRepository.save(user);
    }

    public UserResponse updateProfile(String id, UpdateUserRequest request, String actor) {
        User user = requireUser(id);
        user.setFullName(request.fullName().trim());
        user.setPhone(trimToNull(request.phone()));
        user.registerChange(actor, "ACTUALIZACION DE DATOS");
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse updateRoles(String id, Set<Role> roles, String actor) {
        User user = requireUser(id);
        boolean wasAdmin = user.hasRole(Role.ADMIN);
        boolean keepsAdmin = roles.contains(Role.ADMIN);

        if (wasAdmin && !keepsAdmin) {
            if (id.equals(actor)) {
                throw new BusinessRuleException("No puede retirarse a si mismo el rol de administrador");
            }
            if (user.isActive() && userRepository.countByRolesContainingAndActiveTrue(Role.ADMIN) <= 1) {
                throw new BusinessRuleException("La plataforma quedaria sin administradores activos");
            }
        }

        user.setRoles(new java.util.LinkedHashSet<>(roles));
        user.registerChange(actor, "CAMBIO DE ROLES A " + roles.stream().map(Role::name).sorted().toList());
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse changeStatus(String id, boolean active, String actor) {
        User user = requireUser(id);
        if (!active) {
            if (id.equals(actor)) {
                throw new BusinessRuleException("No puede desactivar su propia cuenta");
            }
            if (user.hasRole(Role.ADMIN) && userRepository.countByRolesContainingAndActiveTrue(Role.ADMIN) <= 1) {
                throw new BusinessRuleException("La plataforma quedaria sin administradores activos");
            }
        }
        user.setActive(active);
        user.registerChange(actor, active ? "CUENTA ACTIVADA" : "CUENTA DESACTIVADA");
        return UserResponse.from(userRepository.save(user));
    }

    public void changePassword(String id, ChangePasswordRequest request, String actor) {
        User user = requireUser(id);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("La contrasena actual no es correcta");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.registerChange(actor, "CAMBIO DE CONTRASENA");
        userRepository.save(user);
    }

    public void resetPassword(String id, String rawPassword, String actor) {
        User user = requireUser(id);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.registerChange(actor, "RESTABLECIMIENTO DE CONTRASENA");
        userRepository.save(user);
    }

    // ---------------------------------------------------------------- apoyo

    public User requireUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una cuenta con el id " + id));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
