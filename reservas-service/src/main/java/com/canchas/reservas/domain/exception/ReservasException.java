package com.canchas.reservas.domain.exception;

public class ReservasException extends RuntimeException {

    private final transient CodigoError codigo;

    public ReservasException(CodigoError codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public CodigoError getCodigo() {
        return codigo;
    }
}
