package com.reservasapp.canchas.infraestructure.driver_adapters.external;

import com.reservasapp.canchas.domain.exception.NegocioException;
import com.reservasapp.canchas.domain.model.UsuarioInfo;
import com.reservasapp.canchas.domain.model.gateway.AuthGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Slf4j
@Component
public class AuthGatewayImpl implements AuthGateway {

    private final RestClient restClient;

    public AuthGatewayImpl(@Qualifier("authRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Optional<UsuarioInfo> validar(Long usuarioId) {
        try {
            AuthValidarResponse respuesta = restClient.get()
                    .uri("/api/auth/{id}/validar", usuarioId)
                    .retrieve()
                    .body(AuthValidarResponse.class);
            if (respuesta == null) {
                throw NegocioException.servicioNoDisponible();
            }
            return Optional.of(new UsuarioInfo(respuesta.id(), respuesta.rol(), respuesta.activo()));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("Fallo al llamar a auth para validar el usuario {}: {}", usuarioId, e.getMessage());
            throw NegocioException.servicioNoDisponible();
        }
    }
}
