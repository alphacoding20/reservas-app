package com.canchas.reservas.infraestructure.driver_adapters.external_repository.dto;

import com.canchas.reservas.domain.model.EstadoCancha;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HorarioResponse(Long canchaId, EstadoCancha estado, LocalTime horaApertura, LocalTime horaCierre,
                              List<BloqueoResponse> bloqueos) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BloqueoResponse(LocalTime horaInicio, LocalTime horaFin, String motivo) {
    }
}
