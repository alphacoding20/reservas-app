package com.reservasapp.canchas.infraestructure.entry_points.dto;

import com.reservasapp.canchas.domain.model.EstadoCancha;
import com.reservasapp.canchas.domain.model.TipoDeporte;

public record CanchaDTO(
        Long id,
        String nombre,
        TipoDeporte tipoDeporte,
        String direccion,
        Double latitud,
        Double longitud,
        Long precioPorHora,
        boolean techada,
        String horaApertura,
        String horaCierre,
        EstadoCancha estado) {
}
