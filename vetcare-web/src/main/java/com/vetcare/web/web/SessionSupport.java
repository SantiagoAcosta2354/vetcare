package com.vetcare.web.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vetcare.web.security.VetcareUser;

/** Acceso al usuario de la sesion desde los controladores. */
final class SessionSupport {

    private SessionSupport() {
    }

    static VetcareUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof VetcareUser user) {
            return user;
        }
        throw new IllegalStateException("No hay un usuario autenticado en la sesion");
    }
}
