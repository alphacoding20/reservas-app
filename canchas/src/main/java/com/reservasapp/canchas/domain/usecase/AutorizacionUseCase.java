package com.reservasapp.canchas.domain.usecase;

import com.reservasapp.canchas.domain.exception.NegocioException;
import com.reservasapp.canchas.domain.model.UsuarioInfo;
import com.reservasapp.canchas.domain.model.gateway.AuthGateway;

public class AutorizacionUseCase {

    private static final String ROL_ADMIN = "ADMIN";

    private final AuthGateway authGateway;

    public AutorizacionUseCase(AuthGateway authGateway) {
        this.authGateway = authGateway;
    }

    public void exigirAdmin(Long usuarioId) {
        if (usuarioId == null) {
            throw NegocioException.sinHeader();
        }
        UsuarioInfo usuario = authGateway.validar(usuarioId).orElseThrow(NegocioException::sinPermiso);
        if (!ROL_ADMIN.equals(usuario.rol()) || !usuario.activo()) {
            throw NegocioException.sinPermiso();
        }
    }
}
