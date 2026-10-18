package com.vetcare.farmacia.client;

import java.net.http.HttpClient;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.vetcare.farmacia.domain.DispensingStatus;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Aviso al servicio de clinica cuando una formula se despacha o queda pendiente.
 *
 * No se firma un token propio: se reenvia el del usuario que hizo el despacho
 * (el auxiliar de farmacia), de modo que el servicio de usuarios sigue siendo el
 * unico que emite credenciales. Si clinica no responde, el despacho ya quedo
 * registrado y solo se deja constancia en el registro.
 */
@Component
public class ClinicaClient {

    private static final Logger log = LoggerFactory.getLogger(ClinicaClient.class);

    private final RestClient restClient;

    public ClinicaClient(@Value("${vetcare.clinica.url}") String baseUrl,
            @Value("${vetcare.downstream-timeout-ms}") int timeoutMs) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(timeoutMs)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public void notifyPrescription(String prescriptionId, DispensingStatus status) {
        if (prescriptionId == null || prescriptionId.isBlank()) {
            return;
        }
        String authorization = currentAuthorization();
        if (authorization == null) {
            return;
        }
        try {
            restClient.patch()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/prescriptions/{id}/status")
                            .queryParam("status", status == DispensingStatus.DESPACHADA ? "DESPACHADA" : "PENDIENTE_POR_STOCK")
                            .build(prescriptionId))
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.warn("No se pudo actualizar la formula {} en clinica: {}", prescriptionId, ex.toString());
        }
    }

    private String currentAuthorization() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }
}
