package com.reservasapp.canchas.infraestructure.mapper;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class Formatos {

    private final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public String hora(LocalTime hora) {
        return hora.format(HORA);
    }

    public String fecha(LocalDate fecha) {
        return fecha.toString();
    }

    public String fechaHora(LocalDateTime fechaHora) {
        return fechaHora.withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
