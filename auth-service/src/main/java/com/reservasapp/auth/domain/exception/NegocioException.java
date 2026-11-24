package com.reservasapp.auth.domain.exception;

import lombok.Getter;

@Getter
public class NegocioException extends RuntimeException {

    private final int status;
    private final String codigo;

    private NegocioException(int status, String codigo, String mensaje) {
        super(mensaje);
        this.status = status;
        this.codigo = codigo;
    }

    public static NegocioException usuarioNoEncontrado() {
        return new NegocioException(404, "USUARIO_NO_ENCONTRADO", "El usuario no existe");
    }

    public static NegocioException credencialesInvalidas() {
        return new NegocioException(401, "CREDENCIALES_INVALIDAS", "Correo o clave incorrectos");
    }

    public static NegocioException correoDuplicado() {
        return new NegocioException(409, "CORREO_DUPLICADO", "El correo ya está registrado");
    }

    public static NegocioException claveIncorrecta() {
        return new NegocioException(400, "CLAVE_INCORRECTA", "La clave actual no coincide");
    }

    public static NegocioException usuarioInactivo() {
        return new NegocioException(403, "USUARIO_INACTIVO", "El usuario está inactivo");
    }

    public static NegocioException sinHeader() {
        return new NegocioException(401, "NO_AUTORIZADO", "Falta el header X-Usuario-Id");
    }

    public static NegocioException sinPermiso() {
        return new NegocioException(403, "NO_AUTORIZADO", "Se requiere un usuario ADMIN activo");
    }
}
