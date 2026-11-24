package com.reservasapp.notificaciones.domain.model;

import java.util.Map;

public record EstadisticasNotificaciones(
        long total,
        long enviadas,
        long fallidas,
        Map<TipoNotificacion, ConteoEnvios> porTipo) {
}
