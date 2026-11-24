package com.canchas.reservas.domain.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record Estadisticas(LocalDate desde, LocalDate hasta, long totalReservas,
                           Map<EstadoReserva, Long> porEstado, List<OcupacionCancha> ocupacionPorCancha) {

    public record OcupacionCancha(Long canchaId, long horasReservadas) {
    }
}
