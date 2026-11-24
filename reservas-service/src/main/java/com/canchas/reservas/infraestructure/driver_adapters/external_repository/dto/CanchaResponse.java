package com.canchas.reservas.infraestructure.driver_adapters.external_repository.dto;

import com.canchas.reservas.domain.model.EstadoCancha;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CanchaResponse(Long id, String nombre, Double latitud, Double longitud, Boolean techada,
                             EstadoCancha estado) {
}
