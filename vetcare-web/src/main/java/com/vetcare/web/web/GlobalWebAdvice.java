package com.vetcare.web.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.vetcare.web.client.ApiException;
import com.vetcare.web.security.VetcareUser;

/** Datos comunes de la vista y presentacion uniforme de los errores de la API. */
@ControllerAdvice
public class GlobalWebAdvice {

    @ModelAttribute("currentUser")
    public VetcareUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof VetcareUser user ? user : null;
    }

    @ExceptionHandler(ApiException.class)
    public String handleApiError(ApiException ex, Model model) {
        model.addAttribute("status", ex.getStatus());
        model.addAttribute("message", ex.getMessage());
        return "error";
    }
}
