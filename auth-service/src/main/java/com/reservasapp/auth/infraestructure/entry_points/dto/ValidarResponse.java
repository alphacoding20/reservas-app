package com.reservasapp.auth.infraestructure.entry_points.dto;

import com.reservasapp.auth.domain.model.Rol;

public record ValidarResponse(
        Long id,
        String nombre,
        String correo,
        Rol rol,
        boolean activo,
        String telegramChatId) {
}
