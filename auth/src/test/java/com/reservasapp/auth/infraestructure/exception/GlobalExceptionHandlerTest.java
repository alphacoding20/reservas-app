package com.reservasapp.auth.infraestructure.exception;

import com.reservasapp.auth.domain.exception.NegocioException;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-12-01T15:15:30Z"), ZoneId.of("America/Bogota"));
        handler = new GlobalExceptionHandler(clock);
        request = new MockHttpServletRequest("GET", "/api/auth/1");
    }

    @Test
    void manejarNegocio_usaStatusCodigoYFormatoDelContrato() {
        ResponseEntity<ErrorRespuesta> respuesta =
                handler.manejarNegocio(NegocioException.correoDuplicado(), request);

        assertEquals(409, respuesta.getStatusCode().value());
        ErrorRespuesta cuerpo = respuesta.getBody();
        assertEquals("2026-12-01T10:15:30", cuerpo.timestamp());
        assertEquals(409, cuerpo.status());
        assertEquals("CORREO_DUPLICADO", cuerpo.error());
        assertEquals("El correo ya está registrado", cuerpo.mensaje());
        assertEquals("/api/auth/1", cuerpo.ruta());
    }

    @Test
    void manejarServlet_metodoNoPermitido_devuelve405() {
        ResponseEntity<ErrorRespuesta> respuesta =
                handler.manejarServlet(new HttpRequestMethodNotSupportedException("POST"), request);

        assertEquals(405, respuesta.getStatusCode().value());
        assertEquals("METODO_NO_PERMITIDO", respuesta.getBody().error());
    }

    @Test
    void manejarServlet_otroErrorServlet_devuelve500() {
        ResponseEntity<ErrorRespuesta> respuesta = handler.manejarServlet(new ServletException("fallo"), request);

        assertEquals(500, respuesta.getStatusCode().value());
        assertEquals("ERROR_INTERNO", respuesta.getBody().error());
    }

    @Test
    void manejarGeneral_devuelve500SinFiltrarElDetalle() {
        ResponseEntity<ErrorRespuesta> respuesta =
                handler.manejarGeneral(new RuntimeException("detalle interno secreto"), request);

        assertEquals(500, respuesta.getStatusCode().value());
        assertEquals("ERROR_INTERNO", respuesta.getBody().error());
        assertEquals("Ocurrió un error interno", respuesta.getBody().mensaje());
    }
}
