package com.reservasapp.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** No levanta el contexto (necesitaría base de datos): solo verifica la clase principal. */
class AuthApplicationTests {

    @Test
    void clasePrincipalEstaAnotadaComoAplicacionSpringBoot() {
        assertTrue(AuthApplication.class.isAnnotationPresent(SpringBootApplication.class));
    }
}
