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

import com.vetcare.clinica.domain.Species;
import com.vetcare.clinica.exception.ResourceNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Consulta de pacientes y citas.
 *
 * No se lee la base del otro servicio: se pide por HTTP con el token del usuario
 * que esta atendiendo, que es el que autoriza la consulta en el servicio de
 * pacientes.
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

    public AppointmentData appointment(String appointmentId) {
        try {
            return restClient.get()
                    .uri("/api/v1/appointments/{id}", appointmentId)
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorization()))
                    .retrieve()
                    .body(AppointmentData.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("No existe una cita con el id " + appointmentId);
        }
    }

    public PetData pet(String petId) {
        try {
            return restClient.get()
                    .uri("/api/v1/pets/{id}", petId)
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorization()))
                    .retrieve()
                    .body(PetData.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("No existe un paciente con el id " + petId);
        }
    }

    /** Verifica que la mascota pertenezca al dueno autenticado (portal). */
    public PetData petFromPortal(String petId) {
        try {
            return restClient.get()
                    .uri("/api/v1/portal/pets/{id}", petId)
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorization()))
                    .retrieve()
                    .body(PetData.class);
        } catch (HttpClientErrorException ex) {
            throw new ResourceNotFoundException("El paciente no pertenece a su cuenta");
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

    public record AppointmentData(String id, String petId, String petName, Species petSpecies, String ownerId,
            String ownerName, String status, String reason) {
    }

    public record PetData(String id, String name, String ownerId, String ownerName, Species species, String status) {
    }
}
