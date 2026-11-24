package com.canchas.reservas.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record HorarioCancha(Long canchaId, LocalDate fecha, EstadoCancha estado, LocalTime horaApertura,
                            LocalTime horaCierre, List<Bloqueo> bloqueos) {

    public HorarioCancha {
        bloqueos = bloqueos == null ? List.of() : List.copyOf(bloqueos);
    }

    public boolean contiene(LocalTime inicio, LocalTime fin) {
        return !inicio.isBefore(horaApertura) && !fin.isAfter(horaCierre);
    }

    public boolean chocaConBloqueo(LocalTime inicio, LocalTime fin) {
        return bloqueos.stream().anyMatch(b -> b.chocaCon(inicio, fin));
    }
}
