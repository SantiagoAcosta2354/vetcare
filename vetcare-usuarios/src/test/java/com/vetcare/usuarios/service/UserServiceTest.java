package com.vetcare.usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vetcare.usuarios.domain.Role;
import com.vetcare.usuarios.domain.User;
import com.vetcare.usuarios.exception.BusinessRuleException;
import com.vetcare.usuarios.exception.InvalidCredentialsException;
import com.vetcare.usuarios.repository.UserRepository;
import com.vetcare.usuarios.web.dto.ChangePasswordRequest;
import com.vetcare.usuarios.web.dto.RegisterRequest;
import com.vetcare.usuarios.web.dto.UserResponse;

/**
 * Pruebas de las reglas que sostienen la administracion de cuentas.
 * No requieren MongoDB ni levantar el contexto: el repositorio se simula.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    private User userWith(String id, boolean active, Role... roles) {
        User user = User.create("Usuario de prueba", "prueba@vetcare.co", null,
                passwordEncoder.encode("Clave1234*"), Set.of(roles), "sistema");
        user.setId(id);
        user.setActive(active);
        return user;
    }

    @Test
    @DisplayName("El registro publico siempre asigna el perfil DUENO")
    void registroPublicoSiempreEsDueno() {
        when(userRepository.existsByEmail("nuevo@vetcare.co")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.register(
                new RegisterRequest("Dueno Nuevo", "Nuevo@VetCare.co", "3001234567", "Clave1234*"));

        assertThat(response.roles()).containsExactly("DUENO");
        assertThat(response.email()).isEqualTo("nuevo@vetcare.co");
    }

    @Test
    @DisplayName("Un correo ya registrado no se puede volver a registrar")
    void correoDuplicadoSeRechaza() {
        when(userRepository.existsByEmail("repetido@vetcare.co")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(
                new RegisterRequest("Dueno Repetido", "repetido@vetcare.co", null, "Clave1234*")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Ya existe una cuenta");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Una contrasena incorrecta devuelve el mismo error que un correo inexistente")
    void contrasenaIncorrectaNoRevelaSiLaCuentaExiste() {
        User user = userWith("u1", true, Role.RECEPCION);
        when(userRepository.findByEmail("prueba@vetcare.co")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.authenticate("prueba@vetcare.co", "otra-clave"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Correo o contrasena incorrectos");
    }

    @Test
    @DisplayName("Una cuenta inactiva no inicia sesion aunque la clave sea correcta")
    void cuentaInactivaNoAutentica() {
        User user = userWith("u1", false, Role.VETERINARIO);
        when(userRepository.findByEmail("prueba@vetcare.co")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.authenticate("prueba@vetcare.co", "Clave1234*"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("El login correcto actualiza la fecha de ultimo acceso")
    void loginCorrectoRegistraUltimoAcceso() {
        User user = userWith("u1", true, Role.FARMACIA);
        when(userRepository.findByEmail("prueba@vetcare.co")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User authenticated = userService.authenticate("prueba@vetcare.co", "Clave1234*");

        assertThat(authenticated.getLastLoginAt()).isNotNull();
        assertThat(authenticated.getRoles()).containsExactly(Role.FARMACIA);
    }

    @Test
    @DisplayName("Un administrador no puede retirarse a si mismo el rol ADMIN")
    void administradorNoSeRetiraSuPropioRol() {
        User admin = userWith("admin-1", true, Role.ADMIN);
        when(userRepository.findById("admin-1")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> userService.updateRoles("admin-1", Set.of(Role.RECEPCION), "admin-1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No puede retirarse a si mismo");
    }

    @Test
    @DisplayName("La plataforma no puede quedarse sin administradores activos")
    void noSePuedeDejarSinAdministradores() {
        User admin = userWith("admin-2", true, Role.ADMIN);
        when(userRepository.findById("admin-2")).thenReturn(Optional.of(admin));
        when(userRepository.countByRolesContainingAndActiveTrue(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> userService.updateRoles("admin-2", Set.of(Role.VETERINARIO), "otro-admin"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("sin administradores activos");
    }

    @Test
    @DisplayName("El cambio de roles guarda autor y fecha en la auditoria")
    void cambioDeRolesGuardaAutorYFecha() {
        User user = userWith("u2", true, Role.RECEPCION);
        when(userRepository.findById("u2")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateRoles("u2", Set.of(Role.RECEPCION, Role.FARMACIA), "admin-1");

        assertThat(response.roles()).containsExactly("FARMACIA", "RECEPCION");
        assertThat(response.audit()).anySatisfy(entry -> {
            assertThat(entry.getActor()).isEqualTo("admin-1");
            assertThat(entry.getAction()).contains("CAMBIO DE ROLES");
            assertThat(entry.getOccurredAt()).isNotNull();
        });
    }

    @Test
    @DisplayName("Nadie puede desactivar su propia cuenta")
    void noSePuedeDesactivarLaCuentaPropia() {
        User admin = userWith("admin-1", true, Role.ADMIN);
        when(userRepository.findById("admin-1")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> userService.changeStatus("admin-1", false, "admin-1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("su propia cuenta");
    }

    @Test
    @DisplayName("El cambio de contrasena exige la contrasena actual")
    void cambioDeContrasenaExigeLaActual() {
        User user = userWith("u3", true, Role.DUENO);
        when(userRepository.findById("u3")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.changePassword("u3",
                new ChangePasswordRequest("clave-equivocada", "Nueva1234*"), "u3"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("La contrasena se guarda como hash BCrypt, nunca en texto plano")
    void contrasenaSeGuardaConHash() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(new RegisterRequest("Dueno Hash", "hash@vetcare.co", null, "Clave1234*"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isNotEqualTo("Clave1234*").startsWith("$2");
    }
}
