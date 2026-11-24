package com.canchas.reservas.infraestructure.driver_adapters.external_repository;

import com.canchas.reservas.domain.exception.CodigoError;
import com.canchas.reservas.domain.model.Rol;
import com.canchas.reservas.domain.model.UsuarioInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static com.canchas.reservas.Fixtures.assertCodigo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UsuarioServiceGatewayImplTest {

    MockRestServiceServer server;
    UsuarioServiceGatewayImpl gateway;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://auth");
        server = MockRestServiceServer.bindTo(builder).build();
        gateway = new UsuarioServiceGatewayImpl(builder.build());
    }

    @Test
    void convierteLaRespuestaDeValidarIgnorandoCamposDesconocidos() {
        server.expect(requestTo("http://auth/api/auth/3/validar")).andRespond(withSuccess(
                "{\"id\":3,\"nombre\":\"Ana Pérez\",\"correo\":\"ana@mail.com\",\"rol\":\"USUARIO\","
                        + "\"activo\":true,\"telegramChatId\":\"123456789\",\"campoNuevo\":\"x\"}",
                MediaType.APPLICATION_JSON));

        UsuarioInfo u = gateway.validar(3L);

        assertEquals(3L, u.id());
        assertEquals("Ana Pérez", u.nombre());
        assertEquals("ana@mail.com", u.correo());
        assertEquals(Rol.USUARIO, u.rol());
        assertTrue(u.activo());
        assertEquals("123456789", u.telegramChatId());
    }

    @Test
    void telegramNuloYUsuarioInactivo() {
        server.expect(requestTo("http://auth/api/auth/4/validar")).andRespond(withSuccess(
                "{\"id\":4,\"nombre\":\"B\",\"correo\":\"b@mail.com\",\"rol\":\"ADMIN\",\"activo\":false,"
                        + "\"telegramChatId\":null}", MediaType.APPLICATION_JSON));

        UsuarioInfo u = gateway.validar(4L);

        assertFalse(u.activo());
        assertNull(u.telegramChatId());
        assertTrue(u.esAdmin());
    }

    @Test
    void sinCampoActivoSeTrataComoInactivo() {
        server.expect(requestTo("http://auth/api/auth/5/validar")).andRespond(withSuccess(
                "{\"id\":5,\"nombre\":\"C\",\"correo\":\"c@mail.com\",\"rol\":\"USUARIO\"}", MediaType.APPLICATION_JSON));
        assertFalse(gateway.validar(5L).activo());
    }

    @Test
    void un404SeConvierteEnUsuarioNoEncontrado() {
        server.expect(requestTo("http://auth/api/auth/9/validar")).andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertCodigo(CodigoError.USUARIO_NO_ENCONTRADO, () -> gateway.validar(9L));
    }

    @Test
    void erroresDelServidorOOtros4xxSeConvierteEnServicioNoDisponible() {
        server.expect(requestTo("http://auth/api/auth/1/validar")).andRespond(withServerError());
        server.expect(requestTo("http://auth/api/auth/2/validar")).andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertCodigo(CodigoError.SERVICIO_NO_DISPONIBLE, () -> gateway.validar(1L));
        assertCodigo(CodigoError.SERVICIO_NO_DISPONIBLE, () -> gateway.validar(2L));
    }

    @Test
    void unTimeoutOFalloDeRedSeConvierteEnServicioNoDisponible() {
        server.expect(requestTo("http://auth/api/auth/1/validar")).andRespond(request -> {
            throw new IOException("Read timed out");
        });
        assertCodigo(CodigoError.SERVICIO_NO_DISPONIBLE, () -> gateway.validar(1L));
    }

    @Test
    void unaRespuestaVaciaEsServicioNoDisponible() {
        server.expect(requestTo("http://auth/api/auth/1/validar")).andRespond(withSuccess());
        assertCodigo(CodigoError.SERVICIO_NO_DISPONIBLE, () -> gateway.validar(1L));
    }
}
