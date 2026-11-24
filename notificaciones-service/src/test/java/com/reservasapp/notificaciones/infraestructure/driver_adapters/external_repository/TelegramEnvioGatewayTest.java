package com.reservasapp.notificaciones.infraestructure.driver_adapters.external_repository;

import com.reservasapp.notificaciones.domain.model.Canal;
import com.reservasapp.notificaciones.domain.model.ResultadoEnvio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Sin internet: MockRestServiceServer simula a Telegram (respuesta, error y timeout). */
class TelegramEnvioGatewayTest {

    private static final String TOKEN = "TEST_TOKEN";
    private static final String URL = "https://api.telegram.org/botTEST_TOKEN/sendMessage";

    private MockRestServiceServer servidor;
    private TelegramEnvioGateway gateway;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.telegram.org");
        servidor = MockRestServiceServer.bindTo(builder).build();
        gateway = new TelegramEnvioGateway(builder.build(), TOKEN);
    }

    @Test
    void soloSoportaTelegram() {
        assertThat(gateway.soporta(Canal.TELEGRAM)).isTrue();
        assertThat(gateway.soporta(Canal.EMAIL)).isFalse();
    }

    @Test
    void enviaChatIdYTextoYDevuelveOkCuandoTelegramRespondeOkTrue() {
        servidor.expect(requestTo(URL))
                .andExpect(method(POST))
                .andExpect(jsonPath("$.chat_id").value("123456789"))
                .andExpect(jsonPath("$.text").value("hola"))
                .andRespond(withSuccess("{\"ok\":true,\"result\":{}}", MediaType.APPLICATION_JSON));

        ResultadoEnvio resultado = gateway.enviar(Canal.TELEGRAM, "123456789", "hola");

        assertThat(resultado.exitoso()).isTrue();
        servidor.verify();
    }

    @Test
    void okFalseEsFallido() {
        servidor.expect(requestTo(URL))
                .andRespond(withSuccess("{\"ok\":false}", MediaType.APPLICATION_JSON));

        assertThat(gateway.enviar(Canal.TELEGRAM, "123", "hola").exitoso()).isFalse();
    }

    @Test
    void cuerpoVacioEsFallido() {
        servidor.expect(requestTo(URL)).andRespond(withSuccess());

        assertThat(gateway.enviar(Canal.TELEGRAM, "123", "hola").exitoso()).isFalse();
    }

    @Test
    void error400DeTelegramEsFallido() {
        servidor.expect(requestTo(URL))
                .andRespond(withStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"ok\":false,\"description\":\"Bad Request: chat not found\"}"));

        assertThat(gateway.enviar(Canal.TELEGRAM, "999", "hola").exitoso()).isFalse();
    }

    @Test
    void error500EsFallido() {
        servidor.expect(requestTo(URL)).andRespond(withServerError());

        assertThat(gateway.enviar(Canal.TELEGRAM, "123", "hola").exitoso()).isFalse();
    }

    @Test
    void timeoutEsFallidoYElDetalleNoFiltraElToken() {
        servidor.expect(requestTo(URL)).andRespond(request -> {
            throw new IOException("Read timed out");
        });

        ResultadoEnvio resultado = gateway.enviar(Canal.TELEGRAM, "123", "hola");

        assertThat(resultado.exitoso()).isFalse();
        assertThat(resultado.detalle()).doesNotContain(TOKEN).contains("***");
    }

    @Test
    void sinTokenConfiguradoFallaSinLlamarATelegram() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.telegram.org");
        MockRestServiceServer sinExpectativas = MockRestServiceServer.bindTo(builder).build();
        TelegramEnvioGateway sinToken = new TelegramEnvioGateway(builder.build(), "  ");

        ResultadoEnvio resultado = sinToken.enviar(Canal.TELEGRAM, "123", "hola");

        assertThat(resultado.exitoso()).isFalse();
        assertThat(resultado.detalle()).contains("TELEGRAM_BOT_TOKEN");
        sinExpectativas.verify(); // no se esperaba ninguna llamada
    }
}
