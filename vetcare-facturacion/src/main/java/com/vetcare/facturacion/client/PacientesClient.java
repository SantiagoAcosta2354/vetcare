package com.vetcare.facturacion.client;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.vetcare.facturacion.exception.BusinessRuleException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resuelve la ficha del dueno autenticado. El portal del dueno no recibe un
 * identificador por la URL: se le pregunta al servicio de pacientes quien es el
 * dueno del token, y con ese identificador se filtran las facturas.
 */
@Component
public class PacientesClient {

    private final RestClient restClient;

    public PacientesClient(@Value("${vetcare.pacientes.url}") String baseUrl,
            @Value("${vetcare.downstream-timeout-ms}") int timeoutMs) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(timeoutMs)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public OwnerData ownerFromPortal() {
        try {
            OwnerData owner = restClient.get()
                    .uri("/api/v1/portal/owner")
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorization()))
                    .retrieve()
                    .body(OwnerData.class);
            if (owner == null || owner.id() == null) {
                throw new BusinessRuleException("La cuenta no tiene ficha de dueno registrada");
            }
            return owner;
        } catch (BusinessRuleException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessRuleException("No fue posible verificar la ficha del dueno en este momento");
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

    public record OwnerData(String id, String fullName) {
    }
}
