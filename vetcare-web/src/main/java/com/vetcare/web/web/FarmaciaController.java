package com.vetcare.web.web;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
@RequestMapping("/farmacia")
public class FarmaciaController {

    private final ApiClient api;

    public FarmaciaController(ApiClient api) {
        this.api = api;
    }

    @GetMapping
    public String panel(Model model) {
        model.addAttribute("pendingPrescriptions", api.list(ApiClient.Service.CLINICA,
                "/api/v1/prescriptions?status=EMITIDA", ApiModels.Prescription.class));
        model.addAttribute("pendingByStock", api.list(ApiClient.Service.CLINICA,
                "/api/v1/prescriptions?status=PENDIENTE_POR_STOCK", ApiModels.Prescription.class));
        model.addAttribute("dispensings", api.list(ApiClient.Service.FARMACIA,
                "/api/v1/dispensings?status=PENDIENTE_POR_STOCK", ApiModels.Dispensing.class));
        model.addAttribute("medications", api.list(ApiClient.Service.FARMACIA, "/api/v1/medications",
                ApiModels.Medication.class));
        model.addAttribute("alerts", api.list(ApiClient.Service.FARMACIA, "/api/v1/alerts", ApiModels.Alert.class));
        return "farmacia";
    }

    /**
     * Despacha la formula completa. Las cantidades salen de la formula, no del
     * formulario: farmacia entrega lo que el veterinario ordeno.
     */
    @PostMapping("/despachar/{prescriptionId}")
    public String despachar(@PathVariable String prescriptionId, RedirectAttributes redirect) {
        ApiModels.Prescription prescription = api.get(ApiClient.Service.CLINICA,
                "/api/v1/prescriptions/" + prescriptionId, ApiModels.Prescription.class);

        List<Map<String, Object>> items = new ArrayList<>();
        for (ApiModels.PrescriptionItem item : prescription.items()) {
            items.add(Map.of("medicationId", item.medicationId(), "quantity", item.quantity()));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("prescriptionId", prescription.id());
        body.put("recordId", prescription.recordId() == null ? "" : prescription.recordId());
        body.put("petId", prescription.petId());
        body.put("petName", prescription.petName());
        body.put("ownerId", prescription.ownerId() == null ? "" : prescription.ownerId());
        body.put("items", items);

        ApiModels.Dispensing dispensing = api.post(ApiClient.Service.FARMACIA, "/api/v1/dispensings", body,
                ApiModels.Dispensing.class);
        if ("DESPACHADA".equals(dispensing.status())) {
            redirect.addFlashAttribute("ok", "Formula despachada y existencia descontada");
        } else {
            redirect.addFlashAttribute("warning", "Sin existencia suficiente: el despacho quedo pendiente y no se desconto inventario");
        }
        return "redirect:/farmacia";
    }

    @PostMapping("/reintentar/{id}")
    public String reintentar(@PathVariable String id, RedirectAttributes redirect) {
        ApiModels.Dispensing dispensing = api.post(ApiClient.Service.FARMACIA,
                "/api/v1/dispensings/" + id + "/retry", Map.of(), ApiModels.Dispensing.class);
        redirect.addFlashAttribute("DESPACHADA".equals(dispensing.status()) ? "ok" : "warning",
                "DESPACHADA".equals(dispensing.status())
                        ? "Despacho completado con la nueva existencia"
                        : "Sigue sin existencia suficiente");
        return "redirect:/farmacia";
    }

    @PostMapping("/compra")
    public String compra(@RequestParam String medicationId,
            @RequestParam String batchNumber,
            @RequestParam String expiresAt,
            @RequestParam int quantity,
            @RequestParam(required = false) String supplier,
            RedirectAttributes redirect) {
        api.post(ApiClient.Service.FARMACIA, "/api/v1/purchases",
                Map.of("medicationId", medicationId,
                        "batchNumber", batchNumber,
                        "expiresAt", expiresAt,
                        "quantity", quantity,
                        "supplier", supplier == null ? "" : supplier),
                Object.class);
        redirect.addFlashAttribute("ok", "Mercancia recibida en el lote " + batchNumber);
        return "redirect:/farmacia";
    }

    @GetMapping("/hoy")
    public String hoy(Model model) {
        model.addAttribute("date", LocalDate.now());
        return "redirect:/farmacia";
    }
}
