package com.reservasapp.canchas.infraestructure.entry_points.dto;

import com.reservasapp.canchas.domain.model.EstadoCancha;

import java.util.List;

public record HorarioDTO(
        Long canchaId,
        String fecha,
        EstadoCancha estado,
        String horaApertura,
        String horaCierre,
        List<BloqueoHoraDTO> bloqueos) {
}
