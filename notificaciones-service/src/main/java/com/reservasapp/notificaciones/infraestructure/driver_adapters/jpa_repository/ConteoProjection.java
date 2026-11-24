package com.reservasapp.notificaciones.infraestructure.driver_adapters.jpa_repository;

import com.reservasapp.notificaciones.domain.model.EstadoNotificacion;
import com.reservasapp.notificaciones.domain.model.TipoNotificacion;

/** Proyeccion del GROUP BY de estadisticas (los alias de la consulta deben coincidir con los getters). */
public interface ConteoProjection {

    TipoNotificacion getTipo();

    EstadoNotificacion getEstado();

    long getCantidad();
}
