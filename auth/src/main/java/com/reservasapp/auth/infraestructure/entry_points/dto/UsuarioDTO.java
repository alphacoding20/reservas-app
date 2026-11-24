package com.reservasapp.auth.infraestructure.entry_points.dto;

import com.reservasapp.auth.domain.model.Rol;

public record UsuarioDTO(
        Long id,
        String nombre,
        String correo,
        Rol rol,
        boolean activo,
        Integer edad,
        String numeroTelefonico,
        String telegramChatId) {
}
