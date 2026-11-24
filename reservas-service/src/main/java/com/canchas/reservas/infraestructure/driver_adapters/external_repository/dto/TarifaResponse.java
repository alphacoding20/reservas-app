package com.canchas.reservas.infraestructure.driver_adapters.external_repository.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TarifaResponse(Long precioPorHora) {
}
