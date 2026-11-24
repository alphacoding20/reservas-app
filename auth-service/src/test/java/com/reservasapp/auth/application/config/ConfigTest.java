package com.reservasapp.auth.application.config;

import com.reservasapp.auth.domain.model.gateway.ClaveGateway;
import com.reservasapp.auth.domain.model.gateway.UsuarioGateway;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class ConfigTest {

    @Test
    void useCaseConfig_creaElCasoDeUso() {
        assertNotNull(new UseCaseConfig().usuarioUseCase(mock(UsuarioGateway.class), mock(ClaveGateway.class)));
    }

    @Test
    void clockConfig_usaZonaHorariaDeBogota() {
        Clock clock = new ClockConfig().clock();

        assertEquals("America/Bogota", clock.getZone().getId());
    }
}
