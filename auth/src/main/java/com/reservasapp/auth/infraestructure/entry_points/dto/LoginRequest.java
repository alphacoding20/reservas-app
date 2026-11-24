package com.reservasapp.auth.infraestructure.entry_points.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String correo, @NotBlank String clave) {
}
