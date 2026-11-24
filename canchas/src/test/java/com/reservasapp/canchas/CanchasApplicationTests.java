package com.reservasapp.canchas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** No levanta el contexto (necesitaría base de datos): solo verifica la clase principal. */
class CanchasApplicationTests {

    @Test
    void clasePrincipalEstaAnotadaComoAplicacionSpringBoot() {
        assertTrue(CanchasApplication.class.isAnnotationPresent(SpringBootApplication.class));
    }
}
