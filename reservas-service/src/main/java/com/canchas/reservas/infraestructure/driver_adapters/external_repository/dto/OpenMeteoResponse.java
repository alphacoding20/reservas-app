package com.canchas.reservas.infraestructure.driver_adapters.external_repository.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoResponse(Hourly hourly) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Hourly(List<String> time,
                         @JsonProperty("precipitation_probability") List<Integer> precipitationProbability) {
    }
}
