package com.reservasapp.notificaciones.domain.usecase;

import com.reservasapp.notificaciones.domain.model.ConteoEnvios;
import com.reservasapp.notificaciones.domain.model.ConteoNotificacion;
import com.reservasapp.notificaciones.domain.model.EstadisticasNotificaciones;
import com.reservasapp.notificaciones.domain.model.EstadoNotificacion;
import com.reservasapp.notificaciones.domain.model.TipoNotificacion;
import com.reservasapp.notificaciones.domain.model.gateway.NotificacionGateway;
import lombok.RequiredArgsConstructor;

import java.util.EnumMap;
import java.util.Map;

@RequiredArgsConstructor
public class EstadisticasNotificacionesUseCase {

    private final NotificacionGateway notificacionGateway;

    /** Enviadas vs fallidas por tipo; siempre incluye los 5 tipos (en cero si no hay datos). */
    public EstadisticasNotificaciones ejecutar() {
        Map<TipoNotificacion, long[]> acumulado = new EnumMap<>(TipoNotificacion.class);
        for (TipoNotificacion tipo : TipoNotificacion.values()) {
            acumulado.put(tipo, new long[2]); // [0] = enviadas, [1] = fallidas
        }

        for (ConteoNotificacion conteo : notificacionGateway.contarPorTipoYEstado()) {
            int posicion = conteo.estado() == EstadoNotificacion.ENVIADA ? 0 : 1;
            acumulado.get(conteo.tipo())[posicion] += conteo.cantidad();
        }

        Map<TipoNotificacion, ConteoEnvios> porTipo = new EnumMap<>(TipoNotificacion.class);
        long enviadas = 0;
        long fallidas = 0;
        for (Map.Entry<TipoNotificacion, long[]> entrada : acumulado.entrySet()) {
            long e = entrada.getValue()[0];
            long f = entrada.getValue()[1];
            porTipo.put(entrada.getKey(), new ConteoEnvios(e, f));
            enviadas += e;
            fallidas += f;
        }
        return new EstadisticasNotificaciones(enviadas + fallidas, enviadas, fallidas, porTipo);
    }
}
