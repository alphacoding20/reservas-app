package com.reservasapp.notificaciones.domain.model;

public class YaEnviadaException extends RuntimeException {

    public YaEnviadaException(Long id) {
        super("La notificación " + id + " ya fue enviada; solo se reenvían las FALLIDAS");
    }
}
