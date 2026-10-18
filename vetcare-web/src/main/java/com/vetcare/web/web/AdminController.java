package com.vetcare.web.web;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vetcare.web.client.ApiClient;
import com.vetcare.web.client.ApiModels;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ApiClient api;

    public AdminController(ApiClient api) {
        this.api = api;
    }

    @GetMapping
    public String usuarios(@RequestParam(defaultValue = "true") boolean includeInactive, Model model) {
        model.addAttribute("users", api.list(ApiClient.Service.USUARIOS,
                "/api/v1/users?includeInactive=" + includeInactive, ApiModels.User.class));
        return "admin";
    }

    @PostMapping
    public String crear(@RequestParam String fullName,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam String password,
            @RequestParam List<String> roles,
            RedirectAttributes redirect) {
        api.post(ApiClient.Service.USUARIOS, "/api/v1/users",
                Map.of("fullName", fullName,
                        "email", email,
                        "phone", phone == null ? "" : phone,
                        "password", password,
                        "roles", roles),
                ApiModels.User.class);
        redirect.addFlashAttribute("ok", "Cuenta creada para " + fullName);
        return "redirect:/admin";
    }

    @PostMapping("/{id}/roles")
    public String roles(@PathVariable String id,
            @RequestParam List<String> roles,
            RedirectAttributes redirect) {
        api.patch(ApiClient.Service.USUARIOS, "/api/v1/users/" + id + "/roles", Map.of("roles", roles),
                ApiModels.User.class);
        redirect.addFlashAttribute("ok", "Roles actualizados");
        return "redirect:/admin";
    }

    @PostMapping("/{id}/estado")
    public String estado(@PathVariable String id,
            @RequestParam boolean active,
            RedirectAttributes redirect) {
        api.patch(ApiClient.Service.USUARIOS, "/api/v1/users/" + id + "/status?active=" + active, null,
                ApiModels.User.class);
        redirect.addFlashAttribute("ok", active ? "Cuenta activada" : "Cuenta desactivada");
        return "redirect:/admin";
    }
}
