package com.canchas.reservas.domain.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record Reserva(
        Long id,
        Long canchaId,
        Long usuarioId,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        Long precioTotal,
        EstadoReserva estado,
        MotivoCancelacion motivoCancelacion,
        LocalDateTime fechaCreacion) {

    public long horas() {
        return Duration.between(horaInicio, horaFin).toHours();
    }

    public LocalDateTime inicio() {
        return LocalDateTime.of(fecha, horaInicio);
    }

    public boolean chocaCon(LocalTime inicio, LocalTime fin) {
        return Franja.chocan(horaInicio, horaFin, inicio, fin);
    }

    public Reserva cancelada(MotivoCancelacion motivo) {
        return new Reserva(id, canchaId, usuarioId, fecha, horaInicio, horaFin, precioTotal,
                EstadoReserva.CANCELADA, motivo, fechaCreacion);
    }

    public Reserva conEstado(EstadoReserva nuevoEstado) {
        return new Reserva(id, canchaId, usuarioId, fecha, horaInicio, horaFin, precioTotal,
                nuevoEstado, motivoCancelacion, fechaCreacion);
    }

    public Reserva reprogramada(LocalDate nuevaFecha, LocalTime nuevaHoraInicio, LocalTime nuevaHoraFin,
                                Long nuevoPrecio) {
        return new Reserva(id, canchaId, usuarioId, nuevaFecha, nuevaHoraInicio, nuevaHoraFin, nuevoPrecio,
                estado, motivoCancelacion, fechaCreacion);
    }
}
