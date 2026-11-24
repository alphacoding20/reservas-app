package com.reservasapp.notificaciones.domain.usecase;

import com.reservasapp.notificaciones.domain.model.EstadoNotificacion;
import com.reservasapp.notificaciones.domain.model.Notificacion;
import com.reservasapp.notificaciones.domain.model.NotificacionNoEncontradaException;
import com.reservasapp.notificaciones.domain.model.YaEnviadaException;
import com.reservasapp.notificaciones.domain.model.gateway.NotificacionGateway;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReenviarNotificacionUseCase {

    private final NotificacionGateway notificacionGateway;
    private final DespachadorEnvio despachador;

    /** Reintenta una notificacion FALLIDA: 404 si no existe, 409 YA_ENVIADA si ya salio, intentos + 1. */
    public Notificacion ejecutar(Long id) {
        Notificacion notificacion = notificacionGateway.buscarPorId(id)
                .orElseThrow(() -> new NotificacionNoEncontradaException(id));

        if (notificacion.getEstado() == EstadoNotificacion.ENVIADA) {
            throw new YaEnviadaException(id);
        }

        EstadoNotificacion estado = despachador.despachar(
                notificacion.getCanal(), notificacion.getDestinatario(), notificacion.getMensaje());
        notificacion.setEstado(estado);
        notificacion.setIntentos(notificacion.getIntentos() + 1);

        // Si vuelve a fallar sigue FALLIDA y se responde 200 igual (el contrato solo define 404 y 409).
        return notificacionGateway.guardar(notificacion);
    }
}
