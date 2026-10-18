package com.vetcare.web.web;

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
@RequestMapping("/examenes")
public class ExamenesController {

    private final ApiClient api;

    public ExamenesController(ApiClient api) {
        this.api = api;
    }

    @GetMapping
    public String pendientes(Model model) {
        model.addAttribute("pending", api.list(ApiClient.Service.CLINICA,
                "/api/v1/lab-orders?status=PENDIENTE", ApiModels.LabOrder.class));
        model.addAttribute("delivered", api.list(ApiClient.Service.CLINICA,
                "/api/v1/lab-orders?status=ENTREGADO", ApiModels.LabOrder.class));
        return "examenes";
    }

    @PostMapping("/{id}/resultado")
    public String registrar(@PathVariable String id,
            @RequestParam String values,
            @RequestParam(required = false) String observations,
            RedirectAttributes redirect) {
        api.post(ApiClient.Service.CLINICA, "/api/v1/lab-orders/" + id + "/result",
                Map.of("values", values, "observations", observations == null ? "" : observations),
                ApiModels.LabOrder.class);
        redirect.addFlashAttribute("ok", "Resultado registrado y orden cerrada");
        return "redirect:/examenes";
    }
}
