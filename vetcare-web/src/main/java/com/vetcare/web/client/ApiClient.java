package com.vetcare.web.client;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import com.vetcare.web.security.VetcareUser;

/**
 * Cliente de los microservicios.
 *
 * Cada llamada reenvia el token de la sesion: la vista no tiene credenciales
 * propias ni consulta las bases de datos. Los errores del servicio se muestran
 * tal como los explico (por ejemplo, "el profesional ya tiene una cita").
 */
@Component
public class ApiClient {

    public enum Service {
        USUARIOS, PACIENTES, CLINICA, FARMACIA, FACTURACION
    }

    private final Map<Service, RestClient> clients = new EnumMap<>(Service.class);

    public ApiClient(@Value("${vetcare.services.usuarios}") String usuarios,
            @Value("${vetcare.services.pacientes}") String pacientes,
            @Value("${vetcare.services.clinica}") String clinica,
            @Value("${vetcare.services.farmacia}") String farmacia,
            @Value("${vetcare.services.facturacion}") String facturacion,
            @Value("${vetcare.services.timeout-ms}") int timeoutMs) {
        clients.put(Service.USUARIOS, build(usuarios, timeoutMs));
        clients.put(Service.PACIENTES, build(pacientes, timeoutMs));
        clients.put(Service.CLINICA, build(clinica, timeoutMs));
        clients.put(Service.FARMACIA, build(farmacia, timeoutMs));
        clients.put(Service.FACTURACION, build(facturacion, timeoutMs));
    }

    private RestClient build(String baseUrl, int timeoutMs) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(timeoutMs)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    // ------------------------------------------------------------- lectura

    public <T> T get(Service service, String path, Class<T> type) {
        return guard(() -> clients.get(service).get().uri(path).headers(this::auth).retrieve().body(type));
    }

    /**
     * Las respuestas de lista se leen como arreglo del tipo pedido: asi el
     * conversor de Spring conoce el tipo real sin depender de un ObjectMapper
     * concreto.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> list(Service service, String path, Class<T> type) {
        return guard(() -> {
            Class<T[]> arrayType = (Class<T[]>) java.lang.reflect.Array.newInstance(type, 0).getClass();
            T[] array = clients.get(service).get().uri(path).headers(this::auth).retrieve().body(arrayType);
            return array == null ? List.<T>of() : List.of(array);
        });
    }

    // ------------------------------------------------------------ escritura

    public <T> T post(Service service, String path, Object body, Class<T> type) {
        return guard(() -> clients.get(service).post().uri(path).headers(this::auth)
                .contentType(MediaType.APPLICATION_JSON).body(body == null ? Map.of() : body)
                .retrieve().body(type));
    }

    public void postVoid(Service service, String path, Object body) {
        guard(() -> clients.get(service).post().uri(path).headers(this::auth)
                .contentType(MediaType.APPLICATION_JSON).body(body == null ? Map.of() : body)
                .retrieve().toBodilessEntity());
    }

    public <T> T patch(Service service, String path, Object body, Class<T> type) {
        return guard(() -> clients.get(service).patch().uri(path).headers(this::auth)
                .contentType(MediaType.APPLICATION_JSON).body(body == null ? Map.of() : body)
                .retrieve().body(type));
    }

    public <T> T put(Service service, String path, Object body, Class<T> type) {
        return guard(() -> clients.get(service).put().uri(path).headers(this::auth)
                .contentType(MediaType.APPLICATION_JSON).body(body == null ? Map.of() : body)
                .retrieve().body(type));
    }

    // --------------------------------------------------------------- apoyo

    private void auth(HttpHeaders headers) {
        headers.setBearerAuth(currentToken());
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    }

    private String currentToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof VetcareUser user) {
            return user.getToken();
        }
        return "";
    }

    private <T> T guard(SupplierWithException<T> call) {
        try {
            return call.get();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new ApiException(ex.getStatusCode().value(), extractMessage(ex.getResponseBodyAsString(),
                    ex.getStatusCode().value()));
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE.value(),
                    "No fue posible comunicarse con el servicio. Intente de nuevo en unos segundos.");
        }
    }

    private String extractMessage(String body, int status) {
        if (body != null && !body.isBlank()) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("\"(?:message|error)\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
                    .matcher(body);
            if (matcher.find()) {
                String message = matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
                if (!message.isBlank()) {
                    return message;
                }
            }
        }
        return switch (status) {
            case 401 -> "Su sesion expiro. Vuelva a iniciar sesion.";
            case 403 -> "No tiene permisos para esta operacion.";
            case 404 -> "No se encontro la informacion solicitada.";
            case 409 -> "La operacion no cumple una regla del sistema.";
            default -> "Ocurrio un error al procesar la solicitud.";
        };
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }
}

