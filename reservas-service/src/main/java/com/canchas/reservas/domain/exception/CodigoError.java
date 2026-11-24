package com.canchas.reservas.domain.exception;

public enum CodigoError {
    // 400
    VALIDACION(400),
    DURACION_INVALIDA(400),
    FECHA_INVALIDA(400),
    FUERA_DE_HORARIO(400),
    // 401 / 403 (el contrato usa el mismo código NO_AUTORIZADO para ambos)
    SIN_CREDENCIALES("NO_AUTORIZADO", 401),
    PERMISO_DENEGADO("NO_AUTORIZADO", 403),
    USUARIO_INACTIVO(403),
    // 404
    USUARIO_NO_ENCONTRADO(404),
    CANCHA_NO_ENCONTRADA(404),
    RESERVA_NO_ENCONTRADA(404),
    // 409
    CANCHA_NO_DISPONIBLE(409),
    FRANJA_OCUPADA(409),
    LIMITE_RESERVAS(409),
    RESERVA_NO_CANCELABLE(409),
    CANCELACION_TARDIA(409),
    ESTADO_RESERVA_INVALIDO(409),
    // 5xx
    ERROR_INTERNO(500),
    SERVICIO_NO_DISPONIBLE(502);

    private final String codigo;
    private final int status;

    CodigoError(int status) {
        this(null, status);
    }

    CodigoError(String codigo, int status) {
        this.codigo = codigo;
        this.status = status;
    }

    public String getCodigo() {
        return codigo != null ? codigo : name();
    }

    public int getStatus() {
        return status;
    }
}
