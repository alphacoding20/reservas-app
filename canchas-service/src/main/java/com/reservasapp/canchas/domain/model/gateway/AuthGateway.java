package com.reservasapp.canchas.domain.model.gateway;

import com.reservasapp.canchas.domain.model.UsuarioInfo;

import java.util.Optional;

public interface AuthGateway {

    Optional<UsuarioInfo> validar(Long usuarioId);
}
