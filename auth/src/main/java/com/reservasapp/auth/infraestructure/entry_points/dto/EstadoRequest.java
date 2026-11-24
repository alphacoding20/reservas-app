package com.reservasapp.auth.infraestructure.entry_points.dto;

import jakarta.validation.constraints.NotNull;

public record EstadoRequest(@NotNull Boolean activo) {
}
