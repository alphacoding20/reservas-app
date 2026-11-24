package com.canchas.reservas.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record Disponibilidad(Long canchaId, LocalDate fecha, List<FranjaDisponible> franjas) {

    public record FranjaDisponible(LocalTime horaInicio, LocalTime horaFin, boolean disponible) {
    }
}
