package com.canchas.reservas.infraestructure.driver_adapters.external_repository;

import com.canchas.reservas.domain.exception.CodigoError;
import com.canchas.reservas.domain.model.CanchaInfo;
import com.canchas.reservas.domain.model.EstadoCancha;
import com.canchas.reservas.domain.model.HorarioCancha;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.LocalDate;

import static com.canchas.reservas.Fixtures.assertCodigo;
import static com.canchas.reservas.Fixtures.h;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CanchaServiceGatewayImplTest {

    static final LocalDate FECHA = LocalDate.of(2026, 12, 8);

    MockRestServiceServer server;
    CanchaServiceGatewayImpl gateway;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://canchas");
        server = MockRestServiceServer.bindTo(builder).build();
        gateway = new CanchaServiceGatewayImpl(builder.build());
    }

    @Test
    void obtenerCanchaConvierteLosCamposQueSeUsan() {
        server.expect(requestTo("http://canchas/api/canchas/1")).andRespond(withSuccess(
                "{\"id\":1,\"nombre\":\"Sintética 1\",\"tipoDeporte\":\"FUTBOL_5\",\"latitud\":4.65,"
                        + "\"longitud\":-74.05,\"precioPorHora\":50000,\"techada\":false,\"horaApertura\":\"06:00\","
                        + "\"horaCierre\":\"22:00\",\"estado\":\"ACTIVA\"}", MediaType.APPLICATION_JSON));

        CanchaInfo c = gateway.obtenerCancha(1L);

        assertEquals("Sintética 1", c.nombre());
        assertEquals(4.65, c.latitud());
        assertEquals(-74.05, c.longitud());
        assertFalse(c.techada());
        assertEquals(EstadoCancha.ACTIVA, c.estado());
    }

    @Test
    void techadaNulaSeTrataComoNoTechada() {
        server.expect(requestTo("http://canchas/api/canchas/2")).andRespond(withSuccess(
                "{\"id\":2,\"nombre\":\"Coliseo\",\"estado\":\"MANTENIMIENTO\"}", MediaType.APPLICATION_JSON));

        CanchaInfo c = gateway.obtenerCancha(2L);

        assertFalse(c.techada());
        assertEquals(EstadoCancha.MANTENIMIENTO, c.estado());
    }

    @Test
    void obtenerHorarioConvierteBloqueosAHoras() {
        server.expect(requestTo("http://canchas/api/canchas/1/horario?fecha=2026-12-08")).andRespond(withSuccess(
                "{\"canchaId\":1,\"fecha\":\"2026-12-08\",\"estado\":\"ACTIVA\",\"horaApertura\":\"06:00\","
                        + "\"horaCierre\":\"22:00\",\"bloqueos\":[{\"horaInicio\":\"14:00\",\"horaFin\":\"16:00\","
                        + "\"motivo\":\"Mantenimiento\"}]}", MediaType.APPLICATION_JSON));

        HorarioCancha horario = gateway.obtenerHorario(1L, FECHA);

        assertEquals(h(6), horario.horaApertura());
        assertEquals(h(22), horario.horaCierre());
        assertEquals(FECHA, horario.fecha());
        assertEquals(1, horario.bloqueos().size());
        assertEquals(h(14), horario.bloqueos().get(0).horaInicio());
        assertEquals("Mantenimiento", horario.bloqueos().get(0).motivo());
        assertTrue(horario.chocaConBloqueo(h(15), h(17)));
    }

    @Test
    void horarioSinBloqueosONuloEsListaVacia() {
        server.expect(requestTo("http://canchas/api/canchas/1/horario?fecha=2026-12-08")).andRespond(withSuccess(
                "{\"canchaId\":1,\"estado\":\"ACTIVA\",\"horaApertura\":\"06:00\",\"horaCierre\":\"22:00\"}",
                MediaType.APPLICATION_JSON));

        assertTrue(gateway.obtenerHorario(1L, FECHA).bloqueos().isEmpty());
    }

    @Test
    void obtenerPrecioPorHoraLeeLaTarifa() {
        server.expect(requestTo("http://canchas/api/canchas/1/tarifa?fecha=2026-12-08")).andRespond(withSuccess(
                "{\"canchaId\":1,\"precioBase\":50000,\"factor\":1.20,\"precioPorHora\":60000,\"esFestivo\":true,"
                        + "\"nombreFestivo\":\"Inmaculada Concepción\"}", MediaType.APPLICATION_JSON));

        assertEquals(60_000L, gateway.obtenerPrecioPorHora(1L, FECHA));
    }

    @Test
    void unaTarifaSinPrecioEsServicioNoDisponible() {
        server.expect(requestTo("http://canchas/api/canchas/1/tarifa?fecha=2026-12-08"))
                .andRespond(withSuccess("{\"canchaId\":1}", MediaType.APPLICATION_JSON));
        assertCodigo(CodigoError.SERVICIO_NO_DISPONIBLE, () -> gateway.obtenerPrecioPorHora(1L, FECHA));
    }

    @Test
    void un404EnCualquierLlamadaEsCanchaNoEncontrada() {
        server.expect(requestTo("http://canchas/api/canchas/9")).andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(requestTo("http://canchas/api/canchas/9/horario?fecha=2026-12-08"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(requestTo("http://canchas/api/canchas/9/tarifa?fecha=2026-12-08"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertCodigo(CodigoError.CANCHA_NO_ENCONTRADA, () -> gateway.obtenerCancha(9L));
        assertCodigo(CodigoError.CANCHA_NO_ENCONTRADA, () -> gateway.obtenerHorario(9L, FECHA));
        assertCodigo(CodigoError.CANCHA_NO_ENCONTRADA, () -> gateway.obtenerPrecioPorHora(9L, FECHA));
    }

    @Test
    void erroresTimeoutYRespuestaVaciaSon502() {
        server.expect(requestTo("http://canchas/api/canchas/1")).andRespond(withServerError());
        server.expect(requestTo("http://canchas/api/canchas/1/horario?fecha=2026-12-08")).andRespond(request -> {
            throw new IOException("timeout");
        });
        server.expect(requestTo("http://canchas/api/canchas/1/tarifa?fecha=2026-12-08")).andRespond(withSuccess());

        assertCodigo(CodigoError.SERVICIO_NO_DISPONIBLE, () -> gateway.obtenerCancha(1L));
        assertCodigo(CodigoError.SERVICIO_NO_DISPONIBLE, () -> gateway.obtenerHorario(1L, FECHA));
        assertCodigo(CodigoError.SERVICIO_NO_DISPONIBLE, () -> gateway.obtenerPrecioPorHora(1L, FECHA));
    }
}
