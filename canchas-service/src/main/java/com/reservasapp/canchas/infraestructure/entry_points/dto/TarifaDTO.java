package com.reservasapp.canchas.infraestructure.entry_points.dto;

import java.math.BigDecimal;

public record TarifaDTO(
        Long canchaId,
        String fecha,
        long precioBase,
        BigDecimal factor,
        long precioPorHora,
        boolean esFestivo,
        boolean esFinDeSemana,
        String nombreFestivo) {
}
