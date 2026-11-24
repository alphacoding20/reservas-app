package com.canchas.reservas.infraestructure.driver_adapters.external_repository;

import com.canchas.reservas.domain.exception.CodigoError;
import com.canchas.reservas.domain.exception.ReservasException;
import com.canchas.reservas.domain.model.UsuarioInfo;
import com.canchas.reservas.domain.model.gateway.UsuarioServiceGateway;
import com.canchas.reservas.infraestructure.driver_adapters.external_repository.dto.UsuarioValidarResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UsuarioServiceGatewayImpl implements UsuarioServiceGateway {

    private final RestClient client;

    public UsuarioServiceGatewayImpl(@Qualifier("authRestClient") RestClient client) {
        this.client = client;
    }

    @Override
    public UsuarioInfo validar(Long usuarioId) {
        UsuarioValidarResponse respuesta;
        try {
            respuesta = client.get()
                    .uri("/api/auth/{id}/validar", usuarioId)
                    .retrieve()
                    .body(UsuarioValidarResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ReservasException(CodigoError.USUARIO_NO_ENCONTRADO, "El usuario " + usuarioId + " no existe");
        } catch (RestClientException e) {
            throw new ReservasException(CodigoError.SERVICIO_NO_DISPONIBLE, "auth-service no disponible");
        }
        if (respuesta == null) {
            throw new ReservasException(CodigoError.SERVICIO_NO_DISPONIBLE, "auth-service devolvió una respuesta vacía");
        }
        return new UsuarioInfo(respuesta.id(), respuesta.nombre(), respuesta.correo(), respuesta.rol(),
                Boolean.TRUE.equals(respuesta.activo()), respuesta.telegramChatId());
    }
}
