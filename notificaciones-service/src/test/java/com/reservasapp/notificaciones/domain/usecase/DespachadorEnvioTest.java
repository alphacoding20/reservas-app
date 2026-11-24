package com.reservasapp.notificaciones.domain.usecase;

import com.reservasapp.notificaciones.domain.model.Canal;
import com.reservasapp.notificaciones.domain.model.EstadoNotificacion;
import com.reservasapp.notificaciones.domain.model.ResultadoEnvio;
import com.reservasapp.notificaciones.domain.model.gateway.EnvioGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DespachadorEnvioTest {

    @Mock
    private EnvioGateway telegram;

    @Test
    void envioExitosoDevuelveEnviada() {
        when(telegram.soporta(Canal.TELEGRAM)).thenReturn(true);
        when(telegram.enviar(Canal.TELEGRAM, "123", "hola")).thenReturn(ResultadoEnvio.ok());

        EstadoNotificacion estado = new DespachadorEnvio(List.of(telegram)).despachar(Canal.TELEGRAM, "123", "hola");

        assertThat(estado).isEqualTo(EstadoNotificacion.ENVIADA);
        verify(telegram).enviar(Canal.TELEGRAM, "123", "hola");
    }

    @Test
    void envioRechazadoDevuelveFallida() {
        when(telegram.soporta(Canal.TELEGRAM)).thenReturn(true);
        when(telegram.enviar(Canal.TELEGRAM, "123", "hola")).thenReturn(ResultadoEnvio.fallido("chat not found"));

        assertThat(new DespachadorEnvio(List.of(telegram)).despachar(Canal.TELEGRAM, "123", "hola"))
                .isEqualTo(EstadoNotificacion.FALLIDA);
    }

    @Test
    void respuestaNulaDevuelveFallida() {
        when(telegram.soporta(Canal.TELEGRAM)).thenReturn(true);
        when(telegram.enviar(Canal.TELEGRAM, "123", "hola")).thenReturn(null);

        assertThat(new DespachadorEnvio(List.of(telegram)).despachar(Canal.TELEGRAM, "123", "hola"))
                .isEqualTo(EstadoNotificacion.FALLIDA);
    }

    @Test
    void siElGatewayLanzaExcepcionNoSePropagaYQuedaFallida() {
        when(telegram.soporta(Canal.TELEGRAM)).thenReturn(true);
        when(telegram.enviar(Canal.TELEGRAM, "123", "hola")).thenThrow(new IllegalStateException("boom"));

        assertThat(new DespachadorEnvio(List.of(telegram)).despachar(Canal.TELEGRAM, "123", "hola"))
                .isEqualTo(EstadoNotificacion.FALLIDA);
    }

    @Test
    void canalSinImplementacionQuedaFallidaSinLlamarAGateways() {
        when(telegram.soporta(Canal.EMAIL)).thenReturn(false);

        assertThat(new DespachadorEnvio(List.of(telegram)).despachar(Canal.EMAIL, "a@b.com", "hola"))
                .isEqualTo(EstadoNotificacion.FALLIDA);
        verify(telegram).soporta(Canal.EMAIL);
    }
}
