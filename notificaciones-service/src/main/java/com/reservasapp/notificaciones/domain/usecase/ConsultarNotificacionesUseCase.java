package com.reservasapp.notificaciones.domain.usecase;

import com.reservasapp.notificaciones.domain.model.Notificacion;
import com.reservasapp.notificaciones.domain.model.NotificacionNoEncontradaException;
import com.reservasapp.notificaciones.domain.model.gateway.NotificacionGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ConsultarNotificacionesUseCase {

    private final NotificacionGateway notificacionGateway;

    public Notificacion porId(Long id) {
        return notificacionGateway.buscarPorId(id)
                .orElseThrow(() -> new NotificacionNoEncontradaException(id));
    }
    /**
     * Historial del usuario, mas recientes primero. Lista vacia si no tiene (no es error).
     */
    public List<Notificacion> porUsuario(Long usuarioId) {
        return notificacionGateway.buscarPorUsuario(usuarioId);
    }

    public void eliminar(Long id) {
        if (!notificacionGateway.existePorId(id)) {
            throw new NotificacionNoEncontradaException(id);
        }
        notificacionGateway.eliminarPorId(id);
    }
}
