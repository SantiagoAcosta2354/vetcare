package com.vetcare.web.web;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.vetcare.web.client.ApiClient;
import com.vetcare.web.client.ApiModels;

@Controller
public class TableroController {

    private final ApiClient api;

    public TableroController(ApiClient api) {
        this.api = api;
    }

    @GetMapping("/tablero")
    public String tablero(Model model) {
        LocalDate today = LocalDate.now();
        model.addAttribute("today", today);
        model.addAttribute("summary", api.get(ApiClient.Service.PACIENTES,
                "/api/v1/appointments/summary?date=" + today, ApiModels.AppointmentSummary.class));
        model.addAttribute("portfolio", api.get(ApiClient.Service.FACTURACION, "/api/v1/portfolio",
                ApiModels.Portfolio.class));
        model.addAttribute("upcoming", api.list(ApiClient.Service.CLINICA,
                "/api/v1/vaccinations/upcoming?days=30", ApiModels.Vaccination.class));
        model.addAttribute("alerts", api.list(ApiClient.Service.FARMACIA, "/api/v1/alerts", ApiModels.Alert.class));
        model.addAttribute("appointments", api.list(ApiClient.Service.PACIENTES,
                "/api/v1/appointments?date=" + today, ApiModels.Appointment.class));
        return "tablero";
    }
}
