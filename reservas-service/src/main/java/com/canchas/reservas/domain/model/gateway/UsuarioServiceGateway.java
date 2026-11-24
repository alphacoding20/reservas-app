package com.canchas.reservas.domain.model.gateway;

import com.canchas.reservas.domain.model.UsuarioInfo;

public interface UsuarioServiceGateway {

    UsuarioInfo validar(Long usuarioId);
}
