package com.vetcare.web.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.vetcare.web.client.ApiClient;
import com.vetcare.web.client.ApiException;
import com.vetcare.web.client.ApiModels;

@Controller
public class PortalController {

    private final ApiClient api;

    public PortalController(ApiClient api) {
        this.api = api;
    }

    /**
     * Portal del dueno. Todo se filtra en los servicios con el token de la
     * sesion: aqui no se pide un identificador de dueno por la URL.
     */
    @GetMapping("/portal")
    public String portal(Model model) {
        ApiModels.Owner owner;
        try {
            owner = api.get(ApiClient.Service.PACIENTES, "/api/v1/portal/owner", ApiModels.Owner.class);
        } catch (ApiException ex) {
            if (ex.isNotFound()) {
                model.addAttribute("sinFicha", true);
                return "portal";
            }
            throw ex;
        }

        List<ApiModels.Pet> pets = api.list(ApiClient.Service.PACIENTES, "/api/v1/portal/pets",
                ApiModels.Pet.class);
        model.addAttribute("owner", owner);
        model.addAttribute("pets", pets);
        model.addAttribute("appointments", api.list(ApiClient.Service.PACIENTES,
                "/api/v1/portal/appointments", ApiModels.Appointment.class));
        model.addAttribute("invoices", api.list(ApiClient.Service.FACTURACION,
                "/api/v1/portal/invoices", ApiModels.Invoice.class));

        Map<String, List<ApiModels.Vaccination>> vaccinationsByPet = new LinkedHashMap<>();
        for (ApiModels.Pet pet : pets) {
            vaccinationsByPet.put(pet.id(), api.list(ApiClient.Service.CLINICA,
                    "/api/v1/vaccinations/portal/" + pet.id(), ApiModels.Vaccination.class));
        }
        model.addAttribute("vaccinationsByPet", vaccinationsByPet);
        return "portal";
    }
}
