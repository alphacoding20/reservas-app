package com.reservasapp.auth.infraestructure.mapper;

import com.reservasapp.auth.domain.model.Usuario;
import com.reservasapp.auth.infraestructure.entry_points.dto.ActualizarUsuarioRequest;
import com.reservasapp.auth.infraestructure.entry_points.dto.CrearUsuarioRequest;
import com.reservasapp.auth.infraestructure.entry_points.dto.LoginResponse;
import com.reservasapp.auth.infraestructure.entry_points.dto.UsuarioDTO;
import com.reservasapp.auth.infraestructure.entry_points.dto.ValidarResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UsuarioDtoMapper {

    public Usuario toUsuario(CrearUsuarioRequest request) {
        return Usuario.builder()
                .nombre(request.nombre())
                .correo(request.correo())
                .clave(request.clave())
                .edad(request.edad())
                .numeroTelefonico(request.numeroTelefonico())
                .telegramChatId(request.telegramChatId())
                .build();
    }

    public Usuario toUsuario(ActualizarUsuarioRequest request) {
        return Usuario.builder()
                .id(request.id())
                .nombre(request.nombre())
                .edad(request.edad())
                .numeroTelefonico(request.numeroTelefonico())
                .telegramChatId(request.telegramChatId())
                .build();
    }

    public UsuarioDTO toDto(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.isActivo(),
                usuario.getEdad(),
                usuario.getNumeroTelefonico(),
                usuario.getTelegramChatId());
    }

    public LoginResponse toLogin(Usuario usuario) {
        return new LoginResponse(usuario.getId(), usuario.getNombre(), usuario.getCorreo(), usuario.getRol());
    }

    public ValidarResponse toValidar(Usuario usuario) {
        return new ValidarResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.isActivo(),
                usuario.getTelegramChatId());
    }
}
