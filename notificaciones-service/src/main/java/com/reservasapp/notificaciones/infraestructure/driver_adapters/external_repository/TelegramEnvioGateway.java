package com.reservasapp.notificaciones.infraestructure.driver_adapters.external_repository;

import com.reservasapp.notificaciones.domain.model.Canal;
import com.reservasapp.notificaciones.domain.model.ResultadoEnvio;
import com.reservasapp.notificaciones.domain.model.gateway.EnvioGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * POST {api}/bot{token}/sendMessage con {chat_id, text}. Respuesta 200 con ok=true = ENVIADA;
 * cualquier otra cosa (ok=false, 4xx/5xx, timeout) = FALLIDA. El RestClient trae timeout de 2 s (AppConfig).
 */
@Component
public class TelegramEnvioGateway implements EnvioGateway {

    private final RestClient restClient;
    private final String botToken;

    public TelegramEnvioGateway(RestClient telegramRestClient,
                                @Value("${telegram.bot-token:}") String botToken) {
        this.restClient = telegramRestClient;
        this.botToken = botToken;
    }

    @Override
    public boolean soporta(Canal canal) {
        return canal == Canal.TELEGRAM;
    }

    @Override
    public ResultadoEnvio enviar(Canal canal, String destinatario, String mensaje) {
        if (!StringUtils.hasText(botToken)) {
            return ResultadoEnvio.fallido("TELEGRAM_BOT_TOKEN no está configurado");
        }
        try {
            Map<?, ?> respuesta = restClient.post()
                    .uri("/bot{token}/sendMessage", botToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("chat_id", destinatario, "text", mensaje))
                    .retrieve()
                    .body(Map.class);

            boolean ok = respuesta != null && Boolean.TRUE.equals(respuesta.get("ok"));
            return ok ? ResultadoEnvio.ok() : ResultadoEnvio.fallido("Telegram respondió sin ok=true");
        } catch (RestClientException e) {
            return ResultadoEnvio.fallido("Error llamando a Telegram: " + sanitizar(e.getMessage()));
        }
    }

    /** La URL de Telegram lleva el token; los mensajes de error de RestClient la incluyen, asi que se oculta. */
    private String sanitizar(String texto) {
        return texto == null ? "sin detalle" : texto.replace(botToken, "***");
    }
}
