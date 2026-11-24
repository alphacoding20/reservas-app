package com.canchas.reservas.domain.usecase;

import com.canchas.reservas.domain.exception.CodigoError;
import com.canchas.reservas.domain.exception.ReservasException;
import com.canchas.reservas.domain.model.UsuarioInfo;
import com.canchas.reservas.domain.model.gateway.UsuarioServiceGateway;

public class ValidadorAdmin {

    private final UsuarioServiceGateway usuarios;

    public ValidadorAdmin(UsuarioServiceGateway usuarios) {
        this.usuarios = usuarios;
    }

    public void exigirAdmin(Long usuarioId) {
        if (usuarioId == null) {
            throw new ReservasException(CodigoError.SIN_CREDENCIALES, "Falta el header X-Usuario-Id");
        }
        UsuarioInfo usuario;
        try {
            usuario = usuarios.validar(usuarioId);
        } catch (ReservasException e) {
            if (e.getCodigo() == CodigoError.USUARIO_NO_ENCONTRADO) {
                throw new ReservasException(CodigoError.PERMISO_DENEGADO, "El usuario no tiene permisos de administrador");
            }
            throw e;
        }
        if (!usuario.activo() || !usuario.esAdmin()) {
            throw new ReservasException(CodigoError.PERMISO_DENEGADO, "Se requiere un usuario ADMIN activo");
        }
    }
}
