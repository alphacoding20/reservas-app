package com.reservasapp.canchas.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record HorarioDia(
        Long canchaId,
        LocalDate fecha,
        EstadoCancha estado,
        LocalTime horaApertura,
        LocalTime horaCierre,
        List<FranjaBloqueada> bloqueos) {
}
