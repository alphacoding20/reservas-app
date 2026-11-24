package com.reservasapp.notificaciones.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;


public record SolicitudNotificacion(
        Long usuarioId,
        String nombreUsuario,
        Canal canal,
        String destinatario,
        Long reservaId,
        String nombreCancha,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        Long precioTotal,
        MotivoCancelacion motivo,
        LocalDate fechaAnterior,
        LocalTime horaInicioAnterior,
        Integer probabilidadLluvia) {
}
