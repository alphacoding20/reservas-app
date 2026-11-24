package com.reservasapp.notificaciones.infraestructure.entry_points.dto;

import java.time.LocalDateTime;

/** Formato unico de error de todos los servicios (contrato 2.4). */
public record ErrorDTO(LocalDateTime timestamp, int status, String error, String mensaje, String ruta) {
}
