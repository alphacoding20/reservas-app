package com.reservasapp.notificaciones.domain.usecase;

import com.reservasapp.notificaciones.domain.model.ConteoEnvios;
import com.reservasapp.notificaciones.domain.model.ConteoNotificacion;
import com.reservasapp.notificaciones.domain.model.EstadisticasNotificaciones;
import com.reservasapp.notificaciones.domain.model.EstadoNotificacion;
import com.reservasapp.notificaciones.domain.model.TipoNotificacion;
import com.reservasapp.notificaciones.domain.model.gateway.NotificacionGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstadisticasNotificacionesUseCaseTest {

    @Mock
    private NotificacionGateway notificacionGateway;

    private EstadisticasNotificacionesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new EstadisticasNotificacionesUseCase(notificacionGateway);
    }

    @Test
    void sumaEnviadasYFallidasTotalesYPorTipo() {
        when(notificacionGateway.contarPorTipoYEstado()).thenReturn(List.of(
                new ConteoNotificacion(TipoNotificacion.CONFIRMACION, EstadoNotificacion.ENVIADA, 5),
                new ConteoNotificacion(TipoNotificacion.CONFIRMACION, EstadoNotificacion.FALLIDA, 1),
                new ConteoNotificacion(TipoNotificacion.ALERTA_LLUVIA, EstadoNotificacion.ENVIADA, 2),
                new ConteoNotificacion(TipoNotificacion.CANCELACION, EstadoNotificacion.FALLIDA, 3)));

        EstadisticasNotificaciones resultado = useCase.ejecutar();

        assertThat(resultado.total()).isEqualTo(11);
        assertThat(resultado.enviadas()).isEqualTo(7);
        assertThat(resultado.fallidas()).isEqualTo(4);
        assertThat(resultado.porTipo().get(TipoNotificacion.CONFIRMACION)).isEqualTo(new ConteoEnvios(5, 1));
        assertThat(resultado.porTipo().get(TipoNotificacion.ALERTA_LLUVIA)).isEqualTo(new ConteoEnvios(2, 0));
        assertThat(resultado.porTipo().get(TipoNotificacion.CANCELACION)).isEqualTo(new ConteoEnvios(0, 3));
    }

    @Test
    void sinDatosDevuelveCeroEnLosCincoTipos() {
        when(notificacionGateway.contarPorTipoYEstado()).thenReturn(List.of());

        EstadisticasNotificaciones resultado = useCase.ejecutar();

        assertThat(resultado.total()).isZero();
        assertThat(resultado.porTipo()).hasSize(TipoNotificacion.values().length);
        assertThat(resultado.porTipo().values()).containsOnly(new ConteoEnvios(0, 0));
    }
}
