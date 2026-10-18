package com.vetcare.web.security;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.vetcare.web.client.ApiModels;

/**
 * Autentica contra el servicio de usuarios. La vista no valida contrasenas: pide
 * el token y, si el servicio responde 401, el acceso se niega con un mensaje
 * generico.
 */
@Component
public class VetcareAuthenticationProvider implements AuthenticationProvider {

    private final RestClient restClient;

    public VetcareAuthenticationProvider(@Value("${vetcare.services.usuarios}") String baseUrl,
            @Value("${vetcare.services.timeout-ms}") int timeoutMs) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(timeoutMs)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials() == null ? "" : authentication.getCredentials().toString();

        try {
            ApiModels.LoginResponse response = restClient.post()
                    .uri("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("email", email, "password", password))
                    .retrieve()
                    .body(ApiModels.LoginResponse.class);

            if (response == null || response.user() == null) {
                throw new BadCredentialsException("Correo o contrasena incorrectos");
            }
            VetcareUser user = new VetcareUser(response.user(), response.accessToken());
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden ex) {
            throw new BadCredentialsException("Correo o contrasena incorrectos");
        } catch (Exception ex) {
            throw new BadCredentialsException("No fue posible conectar con el servicio de usuarios");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
