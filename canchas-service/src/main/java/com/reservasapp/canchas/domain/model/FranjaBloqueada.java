package com.reservasapp.canchas.domain.model;

import java.time.LocalTime;

public record FranjaBloqueada(LocalTime horaInicio, LocalTime horaFin, String motivo) {
}
