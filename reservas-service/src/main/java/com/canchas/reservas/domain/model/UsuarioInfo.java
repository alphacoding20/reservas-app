package com.canchas.reservas.domain.model;

public record UsuarioInfo(Long id, String nombre, String correo, Rol rol, boolean activo, String telegramChatId) {

    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }

    public boolean tieneTelegram() {
        return telegramChatId != null && !telegramChatId.isBlank();
    }
}
