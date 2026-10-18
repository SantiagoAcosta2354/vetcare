package com.vetcare.web.web;

import java.time.LocalDate;
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
import com.vetcare.web.client.ApiException;
import com.vetcare.web.client.ApiModels;
import com.vetcare.web.security.VetcareUser;

@Controller
@RequestMapping("/atencion")
public class AtencionController {

    private final ApiClient api;

    public AtencionController(ApiClient api) {
        this.api = api;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("appointments", api.list(ApiClient.Service.PACIENTES,
                "/api/v1/appointments?date=" + LocalDate.now(), ApiModels.Appointment.class));
        model.addAttribute("date", LocalDate.now());
        return "atencion";
    }

    @GetMapping("/{appointmentId}")
    public String consult(@PathVariable String appointmentId, Model model) {
        ApiModels.Appointment appointment = api.get(ApiClient.Service.PACIENTES,
                "/api/v1/appointments/" + appointmentId, ApiModels.Appointment.class);
        model.addAttribute("appointment", appointment);
        model.addAttribute("record", findRecord(appointmentId));
        return "atencion-cita";
    }

    @PostMapping("/{appointmentId}/abrir")
    public String open(@PathVariable String appointmentId,
            @RequestParam String reason,
            RedirectAttributes redirect) {
        VetcareUser user = SessionSupport.currentUser();
        Map<String, Object> body = Map.of(
                "appointmentId", appointmentId,
                "professionalId", user.getId(),
                "professionalName", user.getFullName(),
                "reason", reason);
        ApiModels.Record record = api.post(ApiClient.Service.CLINICA, "/api/v1/records", body, ApiModels.Record.class);
        redirect.addFlashAttribute("ok", "Historia clinica abierta");
        return "redirect:/atencion/registro/" + record.id();
    }

    @GetMapping("/registro/{recordId}")
    public String record(@PathVariable String recordId, Model model) {
        ApiModels.Record record = api.get(ApiClient.Service.CLINICA, "/api/v1/records/" + recordId,
                ApiModels.Record.class);
        model.addAttribute("record", record);
        model.addAttribute("prescriptions", api.list(ApiClient.Service.CLINICA,
                "/api/v1/prescriptions?recordId=" + recordId, ApiModels.Prescription.class));
        model.addAttribute("labOrders", api.list(ApiClient.Service.CLINICA,
                "/api/v1/lab-orders?petId=" + record.petId(), ApiModels.LabOrder.class));
        model.addAttribute("vaccinations", api.list(ApiClient.Service.CLINICA,
                "/api/v1/vaccinations?petId=" + record.petId(), ApiModels.Vaccination.class));
        model.addAttribute("vaccines", api.list(ApiClient.Service.CLINICA, "/api/v1/vaccines", ApiModels.Vaccine.class));
        model.addAttribute("medications", api.list(ApiClient.Service.FARMACIA, "/api/v1/medications",
                ApiModels.Medication.class));
        return "atencion-registro";
    }

    @PostMapping("/registro/{recordId}/signos")
    public String vitals(@PathVariable String recordId,
            @RequestParam(required = false) Double weightKg,
            @RequestParam(required = false) Double temperatureC,
            @RequestParam(required = false) Integer heartRate,
            @RequestParam(required = false) Integer respiratoryRate,
            RedirectAttributes redirect) {
        // Map.of no admite nulos; los campos opcionales se omiten cuando vienen vacios.
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        putIfPresent(body, "weightKg", weightKg);
        putIfPresent(body, "temperatureC", temperatureC);
        putIfPresent(body, "heartRate", heartRate);
        putIfPresent(body, "respiratoryRate", respiratoryRate);
        api.patch(ApiClient.Service.CLINICA, "/api/v1/records/" + recordId + "/vitals", body,
                ApiModels.Record.class);
        redirect.addFlashAttribute("ok", "Signos vitales registrados");
        return "redirect:/atencion/registro/" + recordId;
    }

    @PostMapping("/registro/{recordId}/diagnostico")
    public String diagnosis(@PathVariable String recordId,
            @RequestParam String diagnosis,
            @RequestParam String treatmentDescription,
            @RequestParam(required = false) Integer treatmentDurationDays,
            @RequestParam(required = false) String treatmentIndications,
            RedirectAttributes redirect) {
        api.patch(ApiClient.Service.CLINICA, "/api/v1/records/" + recordId + "/diagnosis",
                Map.of("diagnosis", diagnosis,
                        "treatmentDescription", treatmentDescription,
                        "treatmentDurationDays", treatmentDurationDays == null ? 1 : treatmentDurationDays,
                        "treatmentIndications", treatmentIndications == null ? "" : treatmentIndications),
                ApiModels.Record.class);
        redirect.addFlashAttribute("ok", "Diagnostico y tratamiento guardados");
        return "redirect:/atencion/registro/" + recordId;
    }

    @PostMapping("/registro/{recordId}/formula")
    public String prescribe(@PathVariable String recordId,
            @RequestParam String medicationId,
            @RequestParam String dose,
            @RequestParam(required = false) String frequency,
            @RequestParam(required = false) Integer durationDays,
            @RequestParam int quantity,
            RedirectAttributes redirect) {
        Map<String, Object> item = Map.of(
                "medicationId", medicationId,
                "dose", dose,
                "frequency", frequency == null ? "" : frequency,
                "durationDays", durationDays == null ? 1 : durationDays,
                "quantity", quantity);
        api.post(ApiClient.Service.CLINICA, "/api/v1/records/" + recordId + "/prescriptions",
                Map.of("items", List.of(item)), ApiModels.Prescription.class);
        redirect.addFlashAttribute("ok", "Formula emitida; farmacia la despacha");
        return "redirect:/atencion/registro/" + recordId;
    }

    @PostMapping("/registro/{recordId}/examen")
    public String labOrder(@PathVariable String recordId,
            @RequestParam String examName,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirect) {
        api.post(ApiClient.Service.CLINICA, "/api/v1/records/" + recordId + "/lab-orders",
                Map.of("examName", examName, "notes", notes == null ? "" : notes), ApiModels.LabOrder.class);
        redirect.addFlashAttribute("ok", "Examen ordenado; queda pendiente hasta el resultado");
        return "redirect:/atencion/registro/" + recordId;
    }

    @PostMapping("/registro/{recordId}/vacuna")
    public String vaccine(@PathVariable String recordId,
            @RequestParam String vaccineId,
            @RequestParam(required = false) String batch,
            @RequestParam String appliedAt,
            RedirectAttributes redirect) {
        api.post(ApiClient.Service.CLINICA, "/api/v1/records/" + recordId + "/vaccinations",
                Map.of("vaccineId", vaccineId, "batch", batch == null ? "" : batch, "appliedAt", appliedAt),
                ApiModels.Vaccination.class);
        redirect.addFlashAttribute("ok", "Vacuna aplicada; la proxima dosis quedo calculada");
        return "redirect:/atencion/registro/" + recordId;
    }

    @PostMapping("/registro/{recordId}/cerrar")
    public String close(@PathVariable String recordId,
            @RequestParam(required = false) String note,
            RedirectAttributes redirect) {
        api.post(ApiClient.Service.CLINICA, "/api/v1/records/" + recordId + "/close",
                Map.of("note", note == null ? "" : note), ApiModels.Record.class);
        redirect.addFlashAttribute("ok", "Historia cerrada");
        return "redirect:/atencion/registro/" + recordId;
    }

    private ApiModels.Record findRecord(String appointmentId) {
        try {
            return api.get(ApiClient.Service.CLINICA, "/api/v1/records/by-appointment/" + appointmentId,
                    ApiModels.Record.class);
        } catch (ApiException ex) {
            if (ex.isNotFound()) {
                return null;
            }
            throw ex;
        }
    }

    private void putIfPresent(Map<String, Object> body, String key, Object value) {
        if (value != null) {
            body.put(key, value);
        }
    }
}
