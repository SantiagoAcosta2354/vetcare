package com.vetcare.clinica.client;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.vetcare.clinica.exception.BusinessRuleException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Verificacion del catalogo de farmacia antes de formular.
 *
 * El veterinario no puede recetar algo que no existe en el catalogo: si lo
 * hiciera, farmacia no podria despacharlo ni facturacion cobrarlo.
 */
@Component
public class FarmaciaClient {

    private final RestClient restClient;

    public FarmaciaClient(@Value("${vetcare.farmacia.url}") String baseUrl,
            @Value("${vetcare.downstream-timeout-ms}") int timeoutMs) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(timeoutMs)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public MedicationData medication(String medicationId) {
        try {
            MedicationData medication = restClient.get()
                    .uri("/api/v1/medications/{id}", medicationId)
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorization()))
                    .retrieve()
                    .body(MedicationData.class);
            if (medication == null) {
                throw new BusinessRuleException("El medicamento no existe en el catalogo de farmacia");
            }
            if (!medication.active()) {
                throw new BusinessRuleException("El medicamento " + medication.name() + " esta retirado del catalogo");
            }
            return medication;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new BusinessRuleException("El medicamento indicado no existe en el catalogo de farmacia");
        }
    }

    private String authorization() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (header != null) {
                return header;
            }
        }
        return "";
    }

    public record MedicationData(String id, String name, String presentation, boolean active) {
    }
}
