package com.reservasapp.notificaciones.infraestructure.entry_points.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.reservasapp.notificaciones.domain.model.Canal;
import com.reservasapp.notificaciones.domain.model.MotivoCancelacion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

/** Cuerpo unico de los 5 endpoints de envio (contrato 6.2). Se ignoran campos desconocidos (contrato 2.2). */
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Obligatorios siempre: usuarioId, nombreUsuario, canal, destinatario, reservaId, nombreCancha, "
        + "fecha, horaInicio, horaFin. Extra: precioTotal (confirmacion), motivo (cancelacion), "
        + "fechaAnterior + horaInicioAnterior (reprogramacion), probabilidadLluvia 0-100 (alerta-lluvia).")
public record SolicitudNotificacionRequest(
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
