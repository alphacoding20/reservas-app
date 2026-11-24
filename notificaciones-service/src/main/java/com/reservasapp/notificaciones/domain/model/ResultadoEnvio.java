package com.reservasapp.notificaciones.domain.model;

public record ResultadoEnvio(boolean exitoso, String detalle) {

    public static ResultadoEnvio ok() {
        return new ResultadoEnvio(true, "enviado");
    }

    public static ResultadoEnvio fallido(String detalle) {
        return new ResultadoEnvio(false, detalle);
    }
}
