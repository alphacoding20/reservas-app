package com.reservasapp.auth.infraestructure.mapper;

import com.reservasapp.auth.domain.model.Usuario;
import com.reservasapp.auth.infraestructure.driver_adapters.jpa_repository.UsuarioData;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UsuarioMapper {

    public Usuario toDomain(UsuarioData data) {
        if (data == null) {
            return null;
        }
        return Usuario.builder()
                .id(data.getId())
                .nombre(data.getNombre())
                .correo(data.getCorreo())
                .clave(data.getClave())
                .edad(data.getEdad())
                .numeroTelefonico(data.getNumeroTelefonico())
                .rol(data.getRol())
                .activo(data.isActivo())
                .telegramChatId(data.getTelegramChatId())
                .build();
    }

    public UsuarioData toData(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return UsuarioData.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .clave(usuario.getClave())
                .edad(usuario.getEdad())
                .numeroTelefonico(usuario.getNumeroTelefonico())
                .rol(usuario.getRol())
                .activo(usuario.isActivo())
                .telegramChatId(usuario.getTelegramChatId())
                .build();
    }
}
