package com.vetcare.web.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.vetcare.web.security.VetcareUser;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /** Cada perfil aterriza en su area de trabajo, no en un menu generico. */
    @GetMapping("/")
    public String home() {
        VetcareUser user = SessionSupport.currentUser();
        if (user.hasRole("ADMIN")) {
            return "redirect:/tablero";
        }
        if (user.hasRole("RECEPCION")) {
            return "redirect:/agenda";
        }
        if (user.hasRole("VETERINARIO")) {
            return "redirect:/atencion";
        }
        if (user.hasRole("LABORATORIO")) {
            return "redirect:/examenes";
        }
        if (user.hasRole("FARMACIA")) {
            return "redirect:/farmacia";
        }
        return "redirect:/portal";
    }
}
