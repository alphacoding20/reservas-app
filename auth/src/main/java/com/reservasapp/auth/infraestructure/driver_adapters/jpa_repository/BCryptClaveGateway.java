package com.reservasapp.auth.infraestructure.driver_adapters.jpa_repository;

import com.reservasapp.auth.domain.model.gateway.ClaveGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BCryptClaveGateway implements ClaveGateway {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String codificar(String clave) {
        return passwordEncoder.encode(clave);
    }

    @Override
    public boolean coincide(String claveEnClaro, String claveCodificada) {
        return passwordEncoder.matches(claveEnClaro, claveCodificada);
    }
}
