package com.reservasapp.notificaciones.domain.model;

public class NotificacionNoEncontradaException extends RuntimeException {

    public NotificacionNoEncontradaException(Long id) {
        super("La notificación " + id + " no existe");
    }
}
