package com.reservasapp.notificaciones.domain.usecase;

import com.reservasapp.notificaciones.TestData;
import com.reservasapp.notificaciones.domain.model.Canal;
import com.reservasapp.notificaciones.domain.model.EstadoNotificacion;
import com.reservasapp.notificaciones.domain.model.Notificacion;
import com.reservasapp.notificaciones.domain.model.NotificacionNoEncontradaException;
import com.reservasapp.notificaciones.domain.model.YaEnviadaException;
import com.reservasapp.notificaciones.domain.model.gateway.NotificacionGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReenviarNotificacionUseCaseTest {

    @Mock
    private NotificacionGateway notificacionGateway;
    @Mock
    private DespachadorEnvio despachador;

    private ReenviarNotificacionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ReenviarNotificacionUseCase(notificacionGateway, despachador);
    }

    @Test
    void reenviaUnaFallidaLaMarcaEnviadaYSumaUnIntento() {
        Notificacion fallida = TestData.notificacion(9L, EstadoNotificacion.FALLIDA);
        when(notificacionGateway.buscarPorId(9L)).thenReturn(Optional.of(fallida));
        when(despachador.despachar(Canal.TELEGRAM, "123456789", "mensaje de prueba"))
                .thenReturn(EstadoNotificacion.ENVIADA);
        when(notificacionGateway.guardar(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Notificacion resultado = useCase.ejecutar(9L);

        assertThat(resultado.getEstado()).isEqualTo(EstadoNotificacion.ENVIADA);
        assertThat(resultado.getIntentos()).isEqualTo(2);
    }

    @Test
    void siVuelveAFallarSigueFallidaConUnIntentoMas() {
        Notificacion fallida = TestData.notificacion(9L, EstadoNotificacion.FALLIDA);
        when(notificacionGateway.buscarPorId(9L)).thenReturn(Optional.of(fallida));
        when(despachador.despachar(any(), any(), any())).thenReturn(EstadoNotificacion.FALLIDA);
        when(notificacionGateway.guardar(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Notificacion resultado = useCase.ejecutar(9L);

        assertThat(resultado.getEstado()).isEqualTo(EstadoNotificacion.FALLIDA);
        assertThat(resultado.getIntentos()).isEqualTo(2);
    }

    @Test
    void siYaFueEnviadaLanzaYaEnviadaYNoVuelveAEnviar() {
        when(notificacionGateway.buscarPorId(9L))
                .thenReturn(Optional.of(TestData.notificacion(9L, EstadoNotificacion.ENVIADA)));

        assertThatThrownBy(() -> useCase.ejecutar(9L)).isInstanceOf(YaEnviadaException.class);

        verify(despachador, never()).despachar(any(), any(), any());
        verify(notificacionGateway, never()).guardar(any());
    }

    @Test
    void siNoExisteLanzaNoEncontrada() {
        when(notificacionGateway.buscarPorId(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(404L)).isInstanceOf(NotificacionNoEncontradaException.class);
    }
}
