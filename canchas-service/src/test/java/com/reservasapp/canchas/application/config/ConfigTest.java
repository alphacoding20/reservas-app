package com.reservasapp.canchas.application.config;

import com.reservasapp.canchas.domain.model.gateway.AuthGateway;
import com.reservasapp.canchas.domain.model.gateway.BloqueoGateway;
import com.reservasapp.canchas.domain.model.gateway.CanchaGateway;
import com.reservasapp.canchas.domain.model.gateway.FestivoGateway;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class ConfigTest {

    @Test
    void useCaseConfig_creaTodosLosCasosDeUso() {
        UseCaseConfig config = new UseCaseConfig();
        CanchaGateway canchaGateway = mock(CanchaGateway.class);
        BloqueoGateway bloqueoGateway = mock(BloqueoGateway.class);

        assertNotNull(config.autorizacionUseCase(mock(AuthGateway.class)));
        assertNotNull(config.canchaUseCase(canchaGateway, bloqueoGateway));
        assertNotNull(config.bloqueoUseCase(canchaGateway, bloqueoGateway));
        assertNotNull(config.horarioUseCase(canchaGateway, bloqueoGateway));
        assertNotNull(config.tarifaUseCase(canchaGateway, mock(FestivoGateway.class), new BigDecimal("1.20")));
    }

    @Test
    void clockConfig_usaZonaHorariaDeBogota() {
        Clock clock = new ClockConfig().clock();

        assertEquals("America/Bogota", clock.getZone().getId());
    }

    @Test
    void httpClientConfig_creaLosClientesHttp() {
        HttpClientConfig config = new HttpClientConfig();

        assertNotNull(config.authRestClient("http://localhost:8081", 2000));
        assertNotNull(config.nagerRestClient("https://date.nager.at/api/v3", 2000));
    }
}
