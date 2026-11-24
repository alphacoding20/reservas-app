package com.reservasapp.auth.infraestructure.entry_points.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ActualizarUsuarioRequest(
        @NotNull Long id,
        @NotBlank @Size(max = 100) String nombre,
        @Min(1) @Max(120) Integer edad,
        @Size(max = 20) String numeroTelefonico,
        @Size(max = 50) String telegramChatId) {
}
