package com.vetcare.web.web;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vetcare.web.client.ApiClient;
import com.vetcare.web.client.ApiModels;

@Controller
@RequestMapping("/agenda")
public class AgendaController {

    private final ApiClient api;

    public AgendaController(ApiClient api) {
        this.api = api;
    }

    @GetMapping
    public String agenda(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        LocalDate day = date == null ? LocalDate.now() : date;
        model.addAttribute("date", day);
        model.addAttribute("appointments",
                api.list(ApiClient.Service.PACIENTES, "/api/v1/appointments?date=" + day, ApiModels.Appointment.class));
        model.addAttribute("clinics", api.list(ApiClient.Service.PACIENTES, "/api/v1/clinics", ApiModels.Clinic.class));
        model.addAttribute("professionals", api.list(ApiClient.Service.USUARIOS,
                "/api/v1/users/staff?role=VETERINARIO", ApiModels.User.class));
        model.addAttribute("owners", api.list(ApiClient.Service.PACIENTES, "/api/v1/owners", ApiModels.Owner.class));
        return "agenda";
    }

    /** Mascotas del dueno seleccionado, para poblar el segundo select del formulario. */
    @GetMapping("/pets")
    @ResponseBody
    public List<ApiModels.Pet> pets(@RequestParam String ownerId) {
        return api.list(ApiClient.Service.PACIENTES, "/api/v1/pets?ownerId=" + ownerId, ApiModels.Pet.class);
    }

    @PostMapping
    public String schedule(@RequestParam String ownerId,
            @RequestParam String petId,
            @RequestParam String clinicId,
            @RequestParam String professionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam String reason,
            RedirectAttributes redirect) {
        ApiModels.User professional = api.list(ApiClient.Service.USUARIOS,
                "/api/v1/users/staff?role=VETERINARIO", ApiModels.User.class).stream()
                .filter(user -> user.id().equals(professionalId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Seleccione un profesional valido"));

        Map<String, Object> body = Map.of(
                "clinicId", clinicId,
                "petId", petId,
                "professionalId", professionalId,
                "professionalName", professional.fullName(),
                "date", date.toString(),
                "startTime", startTime.toString(),
                "reason", reason);

        api.post(ApiClient.Service.PACIENTES, "/api/v1/appointments", body, ApiModels.Appointment.class);
        redirect.addFlashAttribute("ok", "Cita agendada para el " + date + " a las " + startTime);
        return "redirect:/agenda?date=" + date;
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable String id,
            @RequestParam String status,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String date,
            RedirectAttributes redirect) {
        String path = "/api/v1/appointments/" + id + "/status";
        api.patch(ApiClient.Service.PACIENTES, path, Map.of("status", status, "reason", reason == null ? "" : reason),
                ApiModels.Appointment.class);
        redirect.addFlashAttribute("ok", "Estado de la cita actualizado a " + status.toLowerCase().replace('_', ' '));
        return "redirect:/agenda" + (date == null || date.isBlank() ? "" : "?date=" + date);
    }
}
