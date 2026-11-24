package com.reservasapp.canchas.domain.exception;

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

    public static NegocioException canchaNoEncontrada() {
        return new NegocioException(404, "CANCHA_NO_ENCONTRADA", "La cancha no existe");
    }

    public static NegocioException bloqueoNoEncontrado() {
        return new NegocioException(404, "BLOQUEO_NO_ENCONTRADO", "El bloqueo no existe");
    }

    public static NegocioException horarioInvalido(String mensaje) {
        return new NegocioException(400, "HORARIO_INVALIDO", mensaje);
    }

    public static NegocioException sinHeader() {
        return new NegocioException(401, "NO_AUTORIZADO", "Falta el header X-Usuario-Id");
    }

    public static NegocioException sinPermiso() {
        return new NegocioException(403, "NO_AUTORIZADO", "Se requiere un usuario ADMIN activo");
    }

    public static NegocioException servicioNoDisponible() {
        return new NegocioException(502, "SERVICIO_NO_DISPONIBLE", "El servicio auth no está disponible");
    }
}
