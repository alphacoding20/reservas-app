package com.reservasapp.canchas.infraestructure.entry_points.dto;

public record BloqueoDTO(Long id, Long canchaId, String fechaInicio, String fechaFin, String motivo) {
}
