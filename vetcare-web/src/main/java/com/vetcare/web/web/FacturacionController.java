package com.vetcare.web.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
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
@RequestMapping("/facturacion")
public class FacturacionController {

    private final ApiClient api;

    public FacturacionController(ApiClient api) {
        this.api = api;
    }

    @GetMapping
    public String panel(Model model) {
        model.addAttribute("invoices", api.list(ApiClient.Service.FACTURACION, "/api/v1/invoices",
                ApiModels.Invoice.class));
        model.addAttribute("portfolio", api.get(ApiClient.Service.FACTURACION, "/api/v1/portfolio",
                ApiModels.Portfolio.class));
        model.addAttribute("rates", api.list(ApiClient.Service.FACTURACION, "/api/v1/rates", ApiModels.Rate.class));
        model.addAttribute("owners", api.list(ApiClient.Service.PACIENTES, "/api/v1/owners", ApiModels.Owner.class));
        model.addAttribute("today", LocalDate.now());
        return "facturacion";
    }

    @GetMapping("/pets")
    @ResponseBody
    public List<ApiModels.Pet> pets(@RequestParam String ownerId) {
        return api.list(ApiClient.Service.PACIENTES, "/api/v1/pets?ownerId=" + ownerId, ApiModels.Pet.class);
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable String id, Model model) {
        model.addAttribute("invoice", api.get(ApiClient.Service.FACTURACION, "/api/v1/invoices/" + id,
                ApiModels.Invoice.class));
        model.addAttribute("payments", api.list(ApiClient.Service.FACTURACION,
                "/api/v1/invoices/" + id + "/payments", ApiModels.Payment.class));
        return "factura";
    }

    @PostMapping
    public String emitir(@RequestParam String petId,
            @RequestParam String rateId,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate consultationDate,
            RedirectAttributes redirect) {
        ApiModels.Pet pet = api.get(ApiClient.Service.PACIENTES, "/api/v1/pets/" + petId, ApiModels.Pet.class);
        ApiModels.Rate rate = api.list(ApiClient.Service.FACTURACION, "/api/v1/rates", ApiModels.Rate.class).stream()
                .filter(item -> item.id().equals(rateId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Seleccione una tarifa valida"));

        Map<String, Object> item = Map.of(
                "code", rate.code(),
                "description", rate.name(),
                "type", "SERVICIO",
                "quantity", quantity,
                "unitPrice", rate.price());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ownerId", pet.ownerId());
        body.put("ownerName", pet.ownerName());
        body.put("petId", pet.id());
        body.put("petName", pet.name());
        body.put("consultationDate", (consultationDate == null ? LocalDate.now() : consultationDate).toString());
        body.put("items", List.of(item));

        ApiModels.Invoice invoice = api.post(ApiClient.Service.FACTURACION, "/api/v1/invoices", body,
                ApiModels.Invoice.class);
        redirect.addFlashAttribute("ok", "Factura " + invoice.number() + " emitida por " + invoice.total());
        return "redirect:/facturacion/" + invoice.id();
    }

    @PostMapping("/{id}/pago")
    public String pagar(@PathVariable String id,
            @RequestParam BigDecimal amount,
            @RequestParam String method,
            @RequestParam(required = false) String reference,
            RedirectAttributes redirect) {
        api.post(ApiClient.Service.FACTURACION, "/api/v1/invoices/" + id + "/payments",
                Map.of("amount", amount, "method", method, "reference", reference == null ? "" : reference),
                ApiModels.Payment.class);
        redirect.addFlashAttribute("ok", "Pago registrado por " + amount);
        return "redirect:/facturacion/" + id;
    }

    @PostMapping("/{id}/anular")
    public String anular(@PathVariable String id,
            @RequestParam String reason,
            RedirectAttributes redirect) {
        api.post(ApiClient.Service.FACTURACION, "/api/v1/invoices/" + id + "/cancel",
                Map.of("reason", reason), ApiModels.Invoice.class);
        redirect.addFlashAttribute("ok", "Factura anulada");
        return "redirect:/facturacion/" + id;
    }
}
