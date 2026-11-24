package com.reservasapp.canchas.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Tarifa(
        Long canchaId,
        LocalDate fecha,
        long precioBase,
        BigDecimal factor,
        long precioPorHora,
        boolean esFestivo,
        boolean esFinDeSemana,
        String nombreFestivo) {
}
