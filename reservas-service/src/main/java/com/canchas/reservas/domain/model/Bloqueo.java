package com.canchas.reservas.domain.model;

import java.time.LocalTime;

public record Bloqueo(LocalTime horaInicio, LocalTime horaFin, String motivo) {

    public boolean chocaCon(LocalTime inicio, LocalTime fin) {
        return Franja.chocan(horaInicio, horaFin, inicio, fin);
    }
}
