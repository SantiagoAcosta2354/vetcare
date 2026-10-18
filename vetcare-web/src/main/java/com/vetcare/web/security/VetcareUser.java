package com.vetcare.web.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.vetcare.web.client.ApiModels;

/**
 * Usuario de la sesion web. Guarda el token emitido por el servicio de usuarios:
 * la vista nunca guarda la contrasena, y cada llamada a la API reenvia el token.
 */
public class VetcareUser implements UserDetails {

    private final String id;
    private final String fullName;
    private final String email;
    private final List<String> roles;
    private final String token;

    public VetcareUser(ApiModels.User user, String token) {
        this.id = user.id();
        this.fullName = user.fullName();
        this.email = user.email();
        this.roles = user.roles() == null ? List.of() : user.roles();
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getToken() {
        return token;
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
