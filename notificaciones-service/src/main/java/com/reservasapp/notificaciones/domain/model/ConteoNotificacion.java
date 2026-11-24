package com.reservasapp.notificaciones.domain.model;

public record ConteoNotificacion(TipoNotificacion tipo, EstadoNotificacion estado, long cantidad) {
}
