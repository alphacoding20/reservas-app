package com.reservasapp.auth.domain.model.gateway;

import com.reservasapp.auth.domain.model.Usuario;

import java.util.Optional;

public interface UsuarioGateway {
    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorCorreo(String correo);

    boolean existePorCorreo(String correo);

    boolean existePorId(Long id);

    void eliminar(Long id);
}
