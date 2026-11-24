package com.reservasapp.auth.infraestructure.entry_points.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearUsuarioRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Email @Size(max = 150) String correo,
        @NotBlank @Size(min = 6, max = 100) String clave,
        @Min(1) @Max(120) Integer edad,
        @Size(max = 20) String numeroTelefonico,
        @Size(max = 50) String telegramChatId) {
}
