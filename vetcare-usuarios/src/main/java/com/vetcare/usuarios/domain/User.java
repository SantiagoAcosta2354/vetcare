package com.vetcare.usuarios.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Cuenta de usuario de la plataforma.
 *
 * La contrasena nunca se guarda en texto plano: se almacena unicamente su hash
 * BCrypt. El correo tiene indice unico porque es el identificador de inicio de
 * sesion.
 */
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String fullName;

    @Indexed(unique = true)
    private String email;

    private String phone;

    /** Hash BCrypt. Nunca se expone en la API. */
    private String passwordHash;

    private Set<Role> roles = new LinkedHashSet<>();

    private boolean active = true;

    private Instant lastLoginAt;

    /** Quien creo o modifico la cuenta y cuando. */
    private List<AuditEntry> audit = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public User() {
    }

    public static User create(String fullName, String email, String phone, String passwordHash, Set<Role> roles, String actor) {
        User user = new User();
        user.fullName = fullName;
        user.email = email;
        user.phone = phone;
        user.passwordHash = passwordHash;
        user.roles = new LinkedHashSet<>(roles);
        user.active = true;
        user.audit.add(AuditEntry.of(actor, "CREACION"));
        return user;
    }

    public void registerChange(String actor, String action) {
        this.audit.add(AuditEntry.of(actor, action));
    }

    public boolean canAuthenticate() {
        return active;
    }

    public boolean hasRole(Role role) {
        return roles != null && roles.contains(role);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public List<AuditEntry> getAudit() {
        return audit;
    }

    public void setAudit(List<AuditEntry> audit) {
        this.audit = audit;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
