package com.reservasapp.canchas.infraestructure.entry_points.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BloqueoRequest(
        @NotNull LocalDateTime fechaInicio,
        @NotNull LocalDateTime fechaFin,
        @NotBlank @Size(max = 200) String motivo) {
}
