package com.reservasapp.notificaciones.domain.model.gateway;

import com.reservasapp.notificaciones.domain.model.ConteoNotificacion;
import com.reservasapp.notificaciones.domain.model.Notificacion;

import java.util.List;
import java.util.Optional;

public interface NotificacionGateway {

    Notificacion guardar(Notificacion notificacion);

    Optional<Notificacion> buscarPorId(Long id);

    /** Mas recientes primero. */
    List<Notificacion> buscarPorUsuario(Long usuarioId);

    boolean existePorId(Long id);

    void eliminarPorId(Long id);

    List<ConteoNotificacion> contarPorTipoYEstado();
}
