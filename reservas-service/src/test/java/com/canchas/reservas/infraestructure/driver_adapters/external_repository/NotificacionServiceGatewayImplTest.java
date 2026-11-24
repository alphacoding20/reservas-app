package com.canchas.reservas.infraestructure.driver_adapters.external_repository;

import com.canchas.reservas.domain.model.Canal;
import com.canchas.reservas.domain.model.DatosNotificacion;
import com.canchas.reservas.domain.model.MotivoCancelacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static com.canchas.reservas.Fixtures.FUTURO;
import static com.canchas.reservas.Fixtures.h;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class NotificacionServiceGatewayImplTest {

    static final DatosNotificacion DATOS = new DatosNotificacion(3L, "Ana Pérez", Canal.TELEGRAM, "123456789", 55L,
            "Sintética 1", FUTURO, h(18), h(19), 60_000L);

    MockRestServiceServer server;
    NotificacionServiceGatewayImpl gateway;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://notif");
        server = MockRestServiceServer.bindTo(builder).build();
        gateway = new NotificacionServiceGatewayImpl(builder.build());
    }

    private static RequestMatcher cuerpoContiene(String... fragmentos) {
        return request -> {
            String cuerpo = ((MockClientHttpRequest) request).getBodyAsString();
            for (String fragmento : fragmentos) {
                assertThat(cuerpo).contains(fragmento);
            }
        };
    }

    private static RequestMatcher cuerpoNoContiene(String... fragmentos) {
        return request -> {
            String cuerpo = ((MockClientHttpRequest) request).getBodyAsString();
            for (String fragmento : fragmentos) {
                assertThat(cuerpo).doesNotContain(fragmento);
            }
        };
    }

    private void esperar(String ruta, RequestMatcher... matchers) {
        var expectation = server.expect(requestTo("http://notif/api/notificaciones/" + ruta))
                .andExpect(method(HttpMethod.POST));
        for (RequestMatcher m : matchers) {
            expectation.andExpect(m);
        }
        expectation.andRespond(withSuccess());
    }

    private static final String[] BASE = {"\"usuarioId\":3", "\"nombreUsuario\":\"Ana Pérez\"", "\"canal\":\"TELEGRAM\"",
            "\"destinatario\":\"123456789\"", "\"reservaId\":55", "\"nombreCancha\":\"Sintética 1\"",
            "\"fecha\":\"2026-12-01\"", "\"horaInicio\":\"18:00\"", "\"horaFin\":\"19:00\"", "\"precioTotal\":60000"};

    @Test
    void confirmacionEnviaLosCamposComunesYPrecio() {
        esperar("confirmacion", cuerpoContiene(BASE),
                cuerpoNoContiene("motivo", "fechaAnterior", "horaInicioAnterior", "probabilidadLluvia"));
        gateway.enviarConfirmacion(DATOS);
        server.verify();
    }

    @Test
    void recordatorioUsaSuPropiaRuta() {
        esperar("recordatorio", cuerpoContiene(BASE));
        gateway.enviarRecordatorio(DATOS);
        server.verify();
    }

    @Test
    void cancelacionAgregaElMotivo() {
        esperar("cancelacion", cuerpoContiene(BASE), cuerpoContiene("\"motivo\":\"LLUVIA\""));
        gateway.enviarCancelacion(DATOS, MotivoCancelacion.LLUVIA);
        server.verify();
    }

    @Test
    void reprogramacionAgregaLaFranjaAnterior() {
        esperar("reprogramacion", cuerpoContiene(BASE),
                cuerpoContiene("\"fechaAnterior\":\"2026-11-30\"", "\"horaInicioAnterior\":\"09:00\""));
        gateway.enviarReprogramacion(DATOS, FUTURO.minusDays(1), h(9));
        server.verify();
    }

    @Test
    void alertaDeLluviaUsaLaRutaConGuionYLaProbabilidad() {
        esperar("alerta-lluvia", cuerpoContiene(BASE), cuerpoContiene("\"probabilidadLluvia\":75"));
        gateway.enviarAlertaLluvia(DATOS, 75);
        server.verify();
    }

    @Test
    void ningunFalloDeNotificacionesSePropaga() {
        server.expect(requestTo("http://notif/api/notificaciones/confirmacion")).andRespond(withServerError());
        server.expect(requestTo("http://notif/api/notificaciones/recordatorio")).andRespond(request -> {
            throw new IOException("timeout");
        });

        assertDoesNotThrow(() -> gateway.enviarConfirmacion(DATOS));
        assertDoesNotThrow(() -> gateway.enviarRecordatorio(DATOS));
    }
}
