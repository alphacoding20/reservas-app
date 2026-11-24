package com.canchas.reservas.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record DatosNotificacion(Long usuarioId, String nombreUsuario, Canal canal, String destinatario,
                                Long reservaId, String nombreCancha, LocalDate fecha, LocalTime horaInicio,
                                LocalTime horaFin, Long precioTotal) {
}
