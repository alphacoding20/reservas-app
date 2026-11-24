package com.reservasapp.auth.domain.model.gateway;

public interface ClaveGateway {
    String codificar(String clave);

    boolean coincide(String claveEnClaro, String claveCodificada);
}
