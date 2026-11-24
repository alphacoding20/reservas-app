package com.reservasapp.auth.domain.usecase;

import com.reservasapp.auth.domain.exception.NegocioException;
import com.reservasapp.auth.domain.model.Rol;
import com.reservasapp.auth.domain.model.Usuario;
import com.reservasapp.auth.domain.model.gateway.ClaveGateway;
import com.reservasapp.auth.domain.model.gateway.UsuarioGateway;

import java.util.Locale;

public class UsuarioUseCase {

    private final UsuarioGateway usuarioGateway;
    private final ClaveGateway claveGateway;

    public UsuarioUseCase(UsuarioGateway usuarioGateway, ClaveGateway claveGateway) {
        this.usuarioGateway = usuarioGateway;
        this.claveGateway = claveGateway;
    }

    public Usuario registrar(Usuario usuario) {
        String correo = normalizar(usuario.getCorreo());
        if (usuarioGateway.existePorCorreo(correo)) {
            throw NegocioException.correoDuplicado();
        }
        usuario.setId(null);
        usuario.setCorreo(correo);
        usuario.setRol(Rol.USUARIO);
        usuario.setActivo(true);
        usuario.setClave(claveGateway.codificar(usuario.getClave()));
        return usuarioGateway.guardar(usuario);
    }

    public Usuario obtener(Long id) {
        return usuarioGateway.buscarPorId(id).orElseThrow(NegocioException::usuarioNoEncontrado);
    }

    public Usuario buscarPorCorreo(String correo) {
        return usuarioGateway.buscarPorCorreo(normalizar(correo))
                .orElseThrow(NegocioException::usuarioNoEncontrado);
    }

    public Usuario validar(Long id) {
        return obtener(id);
    }

    public Usuario actualizar(Usuario datos) {
        Usuario existente = obtener(datos.getId());
        existente.setNombre(datos.getNombre());
        existente.setEdad(datos.getEdad());
        existente.setNumeroTelefonico(datos.getNumeroTelefonico());
        existente.setTelegramChatId(datos.getTelegramChatId());
        return usuarioGateway.guardar(existente);
    }

    public void eliminar(Long id) {
        if (!usuarioGateway.existePorId(id)) {
            throw NegocioException.usuarioNoEncontrado();
        }
        usuarioGateway.eliminar(id);
    }

    public Usuario login(String correo, String clave) {
        Usuario usuario = usuarioGateway.buscarPorCorreo(normalizar(correo))
                .orElseThrow(NegocioException::credencialesInvalidas);
        if (!claveGateway.coincide(clave, usuario.getClave())) {
            throw NegocioException.credencialesInvalidas();
        }
        if (!usuario.isActivo()) {
            throw NegocioException.usuarioInactivo();
        }
        return usuario;
    }

    public void cambiarClave(Long id, String claveActual, String claveNueva) {
        Usuario usuario = obtener(id);
        if (!claveGateway.coincide(claveActual, usuario.getClave())) {
            throw NegocioException.claveIncorrecta();
        }
        usuario.setClave(claveGateway.codificar(claveNueva));
        usuarioGateway.guardar(usuario);
    }

    public Usuario cambiarRol(Long id, Rol rol) {
        Usuario usuario = obtener(id);
        usuario.setRol(rol);
        return usuarioGateway.guardar(usuario);
    }

    public Usuario cambiarEstado(Long id, boolean activo) {
        Usuario usuario = obtener(id);
        usuario.setActivo(activo);
        return usuarioGateway.guardar(usuario);
    }

    public void exigirAdmin(Long usuarioId) {
        if (usuarioId == null) {
            throw NegocioException.sinHeader();
        }
        Usuario usuario = usuarioGateway.buscarPorId(usuarioId)
                .orElseThrow(NegocioException::sinPermiso);
        if (usuario.getRol() != Rol.ADMIN || !usuario.isActivo()) {
            throw NegocioException.sinPermiso();
        }
    }

    private String normalizar(String correo) {
        return correo == null ? null : correo.trim().toLowerCase(Locale.ROOT);
    }
}
