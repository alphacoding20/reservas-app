package com.reservasapp.notificaciones.domain.usecase;

import com.reservasapp.notificaciones.TestData;
import com.reservasapp.notificaciones.domain.model.EstadoNotificacion;
import com.reservasapp.notificaciones.domain.model.Notificacion;
import com.reservasapp.notificaciones.domain.model.NotificacionNoEncontradaException;
import com.reservasapp.notificaciones.domain.model.gateway.NotificacionGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarNotificacionesUseCaseTest {

    @Mock
    private NotificacionGateway notificacionGateway;

    private ConsultarNotificacionesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ConsultarNotificacionesUseCase(notificacionGateway);
    }

    @Test
    void porIdDevuelveLaNotificacion() {
        Notificacion n = TestData.notificacion(1L, EstadoNotificacion.ENVIADA);
        when(notificacionGateway.buscarPorId(1L)).thenReturn(Optional.of(n));

        assertThat(useCase.porId(1L)).isSameAs(n);
    }

    @Test
    void porIdInexistenteLanzaNoEncontrada() {
        when(notificacionGateway.buscarPorId(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.porId(2L)).isInstanceOf(NotificacionNoEncontradaException.class);
    }

    @Test
    void porUsuarioDevuelveElHistorialTalComoLoEntregaElGateway() {
        List<Notificacion> historial = List.of(
                TestData.notificacion(2L, EstadoNotificacion.ENVIADA),
                TestData.notificacion(1L, EstadoNotificacion.FALLIDA));
        when(notificacionGateway.buscarPorUsuario(3L)).thenReturn(historial);

        assertThat(useCase.porUsuario(3L)).containsExactlyElementsOf(historial);
    }

    @Test
    void porUsuarioSinNotificacionesDevuelveListaVacia() {
        when(notificacionGateway.buscarPorUsuario(99L)).thenReturn(List.of());

        assertThat(useCase.porUsuario(99L)).isEmpty();
    }

    @Test
    void eliminarBorraSiExiste() {
        when(notificacionGateway.existePorId(1L)).thenReturn(true);

        useCase.eliminar(1L);

        verify(notificacionGateway).eliminarPorId(1L);
    }

    @Test
    void eliminarInexistenteLanzaNoEncontradaYNoBorra() {
        when(notificacionGateway.existePorId(1L)).thenReturn(false);

        assertThatThrownBy(() -> useCase.eliminar(1L)).isInstanceOf(NotificacionNoEncontradaException.class);

        verify(notificacionGateway, never()).eliminarPorId(1L);
    }
}
