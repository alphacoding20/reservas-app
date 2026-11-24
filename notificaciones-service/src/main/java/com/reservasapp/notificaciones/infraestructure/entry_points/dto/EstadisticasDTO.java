package com.reservasapp.notificaciones.infraestructure.entry_points.dto;

import java.util.Map;

public record EstadisticasDTO(long total, long enviadas, long fallidas, Map<String, ConteoEnviosDTO> porTipo) {
}
