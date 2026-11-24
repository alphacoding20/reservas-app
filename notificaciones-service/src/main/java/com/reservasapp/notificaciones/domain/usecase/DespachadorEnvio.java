package com.reservasapp.notificaciones.domain.usecase;

import com.reservasapp.notificaciones.domain.model.Canal;
import com.reservasapp.notificaciones.domain.model.EstadoNotificacion;
import com.reservasapp.notificaciones.domain.model.ResultadoEnvio;
import com.reservasapp.notificaciones.domain.model.gateway.EnvioGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * Elige la implementacion de {@link EnvioGateway} que soporta el canal y envia.
 * Regla de oro (guia 5.4): un fallo NUNCA se propaga; se traduce a FALLIDA para poder reenviar.
 */
@Slf4j
@RequiredArgsConstructor
public class DespachadorEnvio {

    private final List<EnvioGateway> envioGateways;

    public EstadoNotificacion despachar(Canal canal, String destinatario, String mensaje) {
        Optional<EnvioGateway> gateway = envioGateways.stream()
                .filter(g -> g.soporta(canal))
                .findFirst();

        if (gateway.isEmpty()) {
            log.warn("Envío fallido: canal no soportado ({})", canal);
            return EstadoNotificacion.FALLIDA;
        }

        try {
            ResultadoEnvio resultado = gateway.get().enviar(canal, destinatario, mensaje);
            if (resultado != null && resultado.exitoso()) {
                return EstadoNotificacion.ENVIADA;
            }
            log.warn("Envío fallido por {}: {}", canal, resultado == null ? "sin respuesta" : resultado.detalle());
        } catch (RuntimeException e) {
            log.warn("Envío fallido por {}: {}", canal, e.getMessage());
        }
        return EstadoNotificacion.FALLIDA;
    }
}
