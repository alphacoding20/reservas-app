package com.canchas.reservas.infraestructure.driver_adapters.external_repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import static com.canchas.reservas.Fixtures.CLOCK;
import static com.canchas.reservas.Fixtures.FUTURO;
import static com.canchas.reservas.Fixtures.h;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ClimaGatewayImplTest {

    static final String RESPUESTA = "{\"latitude\":4.65,\"hourly\":{\"time\":[\"2026-12-01T17:00\","
            + "\"2026-12-01T18:00\",\"2026-12-01T19:00\",\"2026-12-01T20:00\"],"
            + "\"precipitation_probability\":[10,70,40,90]}}";

    MockRestServiceServer server;
    ClimaGatewayImpl gateway;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.open-meteo.com/v1");
        server = MockRestServiceServer.bindTo(builder).build();
        gateway = new ClimaGatewayImpl(builder.build(), CLOCK, 45, 15);
    }

    private void respondeUnaVez(String cuerpo) {
        server.expect(request -> {
            String uri = request.getURI().toString();
            assertThat(uri).startsWith("https://api.open-meteo.com/v1/forecast?");
            assertThat(uri).contains("latitude=4.65", "longitude=-74.05", "hourly=precipitation_probability",
                    "timezone=America/Bogota", "start_date=2026-12-01", "end_date=2026-12-01");
        }).andRespond(withSuccess(cuerpo, MediaType.APPLICATION_JSON));
    }

    @Test
    void devuelveElMaximoDeLasHorasDeLaFranjaSinIncluirLaHoraFinal() {
        respondeUnaVez(RESPUESTA);

        // franja 18:00-20:00 -> horas 18 y 19 -> max(70, 40) = 70 (la hora 20 con 90 no entra)
        assertEquals(Optional.of(70), gateway.consultarProbabilidadLluvia(4.65, -74.05, FUTURO, h(18), h(20)));
        server.verify();
    }

    @Test
    void laSegundaConsultaDeLaMismaFechaYCoordenadasSaleDeLaCache() {
        respondeUnaVez(RESPUESTA); // solo se espera UNA petición HTTP

        assertEquals(Optional.of(70), gateway.consultarProbabilidadLluvia(4.65, -74.05, FUTURO, h(18), h(19)));
        assertEquals(Optional.of(90), gateway.consultarProbabilidadLluvia(4.65, -74.05, FUTURO, h(20), h(21)));
        assertEquals(Optional.of(10), gateway.consultarProbabilidadLluvia(4.65, -74.05, FUTURO, h(17), h(18)));
        server.verify();
    }

    @Test
    void unaFranjaSinDatosDevuelveVacio() {
        respondeUnaVez(RESPUESTA);
        assertTrue(gateway.consultarProbabilidadLluvia(4.65, -74.05, FUTURO, h(3), h(4)).isEmpty());
    }

    @Test
    void fechaFueraDelRangoDeOpenMeteoNoHaceNingunaPeticion() {
        LocalDate lejana = LocalDate.of(2026, 12, 20); // hoy es 23-nov: más de 15 días
        assertTrue(gateway.consultarProbabilidadLluvia(4.65, -74.05, lejana, h(18), h(19)).isEmpty());
        server.verify(); // sin expectativas: cualquier petición habría fallado
    }

    @Test
    void sinCoordenadasNoHayPronostico() {
        assertTrue(gateway.consultarProbabilidadLluvia(null, -74.05, FUTURO, h(18), h(19)).isEmpty());
        assertTrue(gateway.consultarProbabilidadLluvia(4.65, null, FUTURO, h(18), h(19)).isEmpty());
    }

    @Test
    void unErrorHttpDaVacioYNoQuedaGuardadoEnCache() {
        server.expect(request -> { }).andRespond(withServerError());
        respondeUnaVez(RESPUESTA);

        assertTrue(gateway.consultarProbabilidadLluvia(4.65, -74.05, FUTURO, h(18), h(19)).isEmpty());
        assertEquals(Optional.of(70), gateway.consultarProbabilidadLluvia(4.65, -74.05, FUTURO, h(18), h(19)));
    }

    @Test
    void unTimeoutDaVacio() {
        server.expect(request -> { }).andRespond(request -> {
            throw new IOException("Read timed out");
        });
        assertTrue(gateway.consultarProbabilidadLluvia(4.65, -74.05, FUTURO, h(18), h(19)).isEmpty());
    }

    @Test
    void respuestasSinHourlyODatosIncompletosDanVacio() {
        server.expect(request -> { }).andRespond(withSuccess("{\"latitude\":4.65}", MediaType.APPLICATION_JSON));
        server.expect(request -> { }).andRespond(withSuccess("{\"hourly\":{\"time\":[\"2026-12-01T18:00\"]}}", MediaType.APPLICATION_JSON));
        server.expect(request -> { }).andRespond(withSuccess("{\"hourly\":{\"precipitation_probability\":[10]}}", MediaType.APPLICATION_JSON));
        server.expect(request -> { }).andRespond(withSuccess());

        for (int i = 0; i < 4; i++) {
            assertTrue(gateway.consultarProbabilidadLluvia(4.65, -74.05, FUTURO, h(18), h(19)).isEmpty());
        }
    }

    @Test
    void ignoraLasHorasConProbabilidadNula() {
        respondeUnaVez("{\"hourly\":{\"time\":[\"2026-12-01T18:00\",\"2026-12-01T19:00\"],"
                + "\"precipitation_probability\":[null,55]}}");
        assertEquals(Optional.of(55), gateway.consultarProbabilidadLluvia(4.65, -74.05, FUTURO, h(18), h(20)));
    }
}
