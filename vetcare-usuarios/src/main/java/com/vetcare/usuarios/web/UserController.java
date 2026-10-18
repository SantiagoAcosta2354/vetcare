package com.vetcare.usuarios.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vetcare.usuarios.domain.Role;
import com.vetcare.usuarios.service.UserService;
import com.vetcare.usuarios.web.dto.ChangePasswordRequest;
import com.vetcare.usuarios.web.dto.CreateUserRequest;
import com.vetcare.usuarios.web.dto.UpdateRolesRequest;
import com.vetcare.usuarios.web.dto.UpdateUserRequest;
import com.vetcare.usuarios.web.dto.UserResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Usuarios", description = "Cuentas, roles y perfiles del personal")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ------------------------------------------------- perfil propio (/me)

    @GetMapping("/me")
    @Operation(summary = "Devuelve la cuenta del usuario autenticado")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return userService.findById(jwt.getSubject());
    }

    @PatchMapping("/me")
    @Operation(summary = "Actualiza el nombre y el telefono de la cuenta propia")
    public UserResponse updateMe(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateProfile(jwt.getSubject(), request, jwt.getSubject());
    }

    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cambia la contrasena propia exigiendo la actual")
    public void changeMyPassword(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(jwt.getSubject(), request, jwt.getSubject());
    }

    // ------------------------------------------------------------- personal

    @GetMapping("/staff")
    @Operation(summary = "Lista el personal activo de un rol (por ejemplo, los veterinarios de la agenda)")
    public List<UserResponse> staff(@RequestParam Role role) {
        return userService.findByRole(role);
    }

    // -------------------------------------------------------- administracion

    @GetMapping
    @Operation(summary = "Lista las cuentas registradas (solo administrador)")
    public List<UserResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return userService.findAll(includeInactive);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta una cuenta por su identificador (solo administrador)")
    public UserResponse findById(@PathVariable String id) {
        return userService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea una cuenta con uno o varios perfiles (solo administrador)")
    public UserResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateUserRequest request) {
        return userService.create(request, jwt.getSubject());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualiza los datos de presentacion de una cuenta (solo administrador)")
    public UserResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateProfile(id, request, jwt.getSubject());
    }

    @PatchMapping("/{id}/roles")
    @Operation(summary = "Asigna los roles de una cuenta (solo administrador)")
    public UserResponse updateRoles(@AuthenticationPrincipal Jwt jwt, @PathVariable String id,
            @Valid @RequestBody UpdateRolesRequest request) {
        return userService.updateRoles(id, request.roles(), jwt.getSubject());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activa o desactiva una cuenta (solo administrador)")
    public UserResponse changeStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable String id,
            @RequestParam boolean active) {
        return userService.changeStatus(id, active, jwt.getSubject());
    }

    @PostMapping("/{id}/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Restablece la contrasena de una cuenta (solo administrador)")
    public void resetPassword(@AuthenticationPrincipal Jwt jwt, @PathVariable String id,
            @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request.newPassword(), jwt.getSubject());
    }

    /** Cuerpo minimo del restablecimiento de contrasena. */
    public record ResetPasswordRequest(String newPassword) {
    }
}
