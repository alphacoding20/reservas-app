package com.reservasapp.notificaciones.infraestructure.entry_points.dto;

import com.reservasapp.notificaciones.domain.model.Canal;
import com.reservasapp.notificaciones.domain.model.EstadoNotificacion;
import com.reservasapp.notificaciones.domain.model.TipoNotificacion;

import java.time.LocalDateTime;

public record NotificacionDTO(
        Long id,
        Long usuarioId,
        Canal canal,
        String destinatario,
        TipoNotificacion tipo,
        String mensaje,
        EstadoNotificacion estado,
        int intentos,
        LocalDateTime fecha) {
}
