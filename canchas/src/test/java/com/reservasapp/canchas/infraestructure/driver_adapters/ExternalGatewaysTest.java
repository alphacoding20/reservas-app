package com.reservasapp.canchas.infraestructure.driver_adapters;

import com.reservasapp.canchas.domain.exception.NegocioException;
import com.reservasapp.canchas.domain.model.Festivo;
import com.reservasapp.canchas.domain.model.UsuarioInfo;
import com.reservasapp.canchas.infraestructure.driver_adapters.external.AuthGatewayImpl;
import com.reservasapp.canchas.infraestructure.driver_adapters.external.NagerFestivoGateway;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Prueban los clientes HTTP salientes contra un servidor simulado (no hace llamadas reales). */
class ExternalGatewaysTest {

    private static final String AUTH_URL = "http://auth.test";
    private static final String NAGER_URL = "https://nager.test";

    // ---------- AuthGatewayImpl ----------

    private RestClient.Builder authBuilder() {
        return RestClient.builder().baseUrl(AUTH_URL);
    }

    @Test
    void auth_validar_ok_devuelveElUsuario() {
        RestClient.Builder builder = authBuilder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(AUTH_URL + "/api/auth/1/validar"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"id":1,"nombre":"Administrador","correo":"admin@canchas.com",
                         "rol":"ADMIN","activo":true,"telegramChatId":null}
                        """, MediaType.APPLICATION_JSON));

        Optional<UsuarioInfo> resultado = new AuthGatewayImpl(builder.build()).validar(1L);

        assertEquals(new UsuarioInfo(1L, "ADMIN", true), resultado.orElseThrow());
        server.verify();
    }

    @Test
    void auth_validar_usuarioNoExiste_devuelveVacio() {
        RestClient.Builder builder = authBuilder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(AUTH_URL + "/api/auth/9/validar")).andRespond(withResourceNotFound());

        assertTrue(new AuthGatewayImpl(builder.build()).validar(9L).isEmpty());
        server.verify();
    }

    @Test
    void auth_validar_errorDelServidor_lanza502() {
        RestClient.Builder builder = authBuilder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(AUTH_URL + "/api/auth/1/validar")).andRespond(withServerError());
        AuthGatewayImpl gateway = new AuthGatewayImpl(builder.build());

        NegocioException ex = assertThrows(NegocioException.class, () -> gateway.validar(1L));

        assertEquals(502, ex.getStatus());
        assertEquals("SERVICIO_NO_DISPONIBLE", ex.getCodigo());
    }

    @Test
    void auth_validar_respuestaVacia_lanza502() {
        RestClient.Builder builder = authBuilder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(AUTH_URL + "/api/auth/1/validar")).andRespond(withSuccess());
        AuthGatewayImpl gateway = new AuthGatewayImpl(builder.build());

        NegocioException ex = assertThrows(NegocioException.class, () -> gateway.validar(1L));

        assertEquals("SERVICIO_NO_DISPONIBLE", ex.getCodigo());
    }

    // ---------- NagerFestivoGateway ----------

    private static final String JSON_FESTIVOS = """
            [{"date":"2026-01-01","localName":"Año Nuevo","name":"New Year's Day","countryCode":"CO"},
             {"date":"2026-12-08","localName":"Inmaculada Concepción","name":"Immaculate Conception","countryCode":"CO"},
             {"date":null,"localName":"Sin fecha","name":"x","countryCode":"CO"}]
            """;

    @Test
    void nager_festivosDelAnio_ok_ignoraLosElementosSinFecha() {
        RestClient.Builder builder = RestClient.builder().baseUrl(NAGER_URL);
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(NAGER_URL + "/PublicHolidays/2026/CO"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(JSON_FESTIVOS, MediaType.APPLICATION_JSON));

        List<Festivo> festivos = new NagerFestivoGateway(builder.build()).festivosDelAnio(2026);

        assertEquals(List.of(
                new Festivo(LocalDate.of(2026, 1, 1), "Año Nuevo"),
                new Festivo(LocalDate.of(2026, 12, 8), "Inmaculada Concepción")), festivos);
        server.verify();
    }

    @Test
    void nager_festivosDelAnio_cacheaPorAnio_unaSolaLlamada() {
        RestClient.Builder builder = RestClient.builder().baseUrl(NAGER_URL);
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), requestTo(NAGER_URL + "/PublicHolidays/2026/CO"))
                .andRespond(withSuccess(JSON_FESTIVOS, MediaType.APPLICATION_JSON));
        NagerFestivoGateway gateway = new NagerFestivoGateway(builder.build());

        List<Festivo> primera = gateway.festivosDelAnio(2026);
        List<Festivo> segunda = gateway.festivosDelAnio(2026);

        assertEquals(primera, segunda);
        server.verify();
    }

    @Test
    void nager_festivosDelAnio_siFalla_devuelveVacioYNoCacheaElFallo() {
        RestClient.Builder builder = RestClient.builder().baseUrl(NAGER_URL);
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), requestTo(NAGER_URL + "/PublicHolidays/2026/CO")).andRespond(withServerError());
        server.expect(once(), requestTo(NAGER_URL + "/PublicHolidays/2026/CO"))
                .andRespond(withSuccess(JSON_FESTIVOS, MediaType.APPLICATION_JSON));
        NagerFestivoGateway gateway = new NagerFestivoGateway(builder.build());

        assertTrue(gateway.festivosDelAnio(2026).isEmpty());
        assertEquals(2, gateway.festivosDelAnio(2026).size());
        server.verify();
    }

    @Test
    void nager_festivosDelAnio_respuestaVacia_devuelveVacio() {
        RestClient.Builder builder = RestClient.builder().baseUrl(NAGER_URL);
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(NAGER_URL + "/PublicHolidays/2027/CO")).andRespond(withSuccess());

        assertTrue(new NagerFestivoGateway(builder.build()).festivosDelAnio(2027).isEmpty());
    }
}
