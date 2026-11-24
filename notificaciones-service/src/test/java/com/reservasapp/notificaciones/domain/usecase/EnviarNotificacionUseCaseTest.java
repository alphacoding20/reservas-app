package com.reservasapp.notificaciones.domain.usecase;

import com.reservasapp.notificaciones.TestData;
import com.reservasapp.notificaciones.domain.model.Canal;
import com.reservasapp.notificaciones.domain.model.EstadoNotificacion;
import com.reservasapp.notificaciones.domain.model.Notificacion;
import com.reservasapp.notificaciones.domain.model.SolicitudNotificacion;
import com.reservasapp.notificaciones.domain.model.TipoNotificacion;
import com.reservasapp.notificaciones.domain.model.ValidacionException;
import com.reservasapp.notificaciones.domain.model.gateway.NotificacionGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnviarNotificacionUseCaseTest {

    @Mock
    private NotificacionGateway notificacionGateway;
    @Mock
    private DespachadorEnvio despachador;

    private EnviarNotificacionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new EnviarNotificacionUseCase(notificacionGateway, despachador, TestData.CLOCK);
    }

    @Test
    void envioExitosoGuardaEnviadaConUnIntentoYFechaDeBogota() {
        when(despachador.despachar(eq(Canal.TELEGRAM), eq("123456789"), any())).thenReturn(EstadoNotificacion.ENVIADA);
        when(notificacionGateway.guardar(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Notificacion resultado = useCase.ejecutar(TipoNotificacion.CONFIRMACION, TestData.solicitudCompleta());

        assertThat(resultado.getEstado()).isEqualTo(EstadoNotificacion.ENVIADA);
        assertThat(resultado.getIntentos()).isEqualTo(1);
        assertThat(resultado.getFecha()).isEqualTo(TestData.AHORA);
        assertThat(resultado.getUsuarioId()).isEqualTo(3L);
        assertThat(resultado.getTipo()).isEqualTo(TipoNotificacion.CONFIRMACION);
        assertThat(resultado.getDestinatario()).isEqualTo("123456789");
        assertThat(resultado.getMensaje()).contains("Ana Pérez", "$60.000 COP");
    }

    @Test
    void siElEnvioFallaSeGuardaFallidaYNoSeLanzaError() {
        when(despachador.despachar(any(), any(), any())).thenReturn(EstadoNotificacion.FALLIDA);
        when(notificacionGateway.guardar(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Notificacion resultado = useCase.ejecutar(TipoNotificacion.RECORDATORIO, TestData.solicitudCompleta());

        assertThat(resultado.getEstado()).isEqualTo(EstadoNotificacion.FALLIDA);
        ArgumentCaptor<Notificacion> guardada = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionGateway).guardar(guardada.capture());
        assertThat(guardada.getValue().getEstado()).isEqualTo(EstadoNotificacion.FALLIDA);
    }

    @Test
    void siFaltanCamposObligatoriosLanzaValidacionYNoEnviaNiGuarda() {
        SolicitudNotificacion incompleta = new SolicitudNotificacion(null, " ", null, "", null, null,
                null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> useCase.ejecutar(TipoNotificacion.RECORDATORIO, incompleta))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContainingAll("usuarioId", "nombreUsuario", "canal", "destinatario", "reservaId",
                        "nombreCancha", "fecha", "horaInicio", "horaFin");

        verify(despachador, never()).despachar(any(), any(), any());
        verify(notificacionGateway, never()).guardar(any());
    }

    @Test
    void confirmacionExigePrecioTotal() {
        SolicitudNotificacion s = con(TestData.solicitudCompleta(), null, null, null, null);

        assertThatThrownBy(() -> useCase.ejecutar(TipoNotificacion.CONFIRMACION, s))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("precioTotal");
    }

    @Test
    void cancelacionExigeMotivo() {
        SolicitudNotificacion s = con(TestData.solicitudCompleta(), 60000L, null, null, null);

        assertThatThrownBy(() -> useCase.ejecutar(TipoNotificacion.CANCELACION, s))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("motivo");
    }

    @Test
    void reprogramacionExigeFechaYHoraAnteriores() {
        SolicitudNotificacion s = con(TestData.solicitudCompleta(), 60000L, null, null, null);

        assertThatThrownBy(() -> useCase.ejecutar(TipoNotificacion.REPROGRAMACION, s))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("fechaAnterior")
                .hasMessageContaining("horaInicioAnterior");
    }

    @Test
    void alertaLluviaExigeProbabilidad() {
        SolicitudNotificacion s = con(TestData.solicitudCompleta(), 60000L, null, null, null);

        assertThatThrownBy(() -> useCase.ejecutar(TipoNotificacion.ALERTA_LLUVIA, s))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("probabilidadLluvia");
    }

    @Test
    void alertaLluviaRechazaProbabilidadFueraDeRango() {
        SolicitudNotificacion alta = con(TestData.solicitudCompleta(), null, null, null, 101);
        SolicitudNotificacion baja = con(TestData.solicitudCompleta(), null, null, null, -1);

        assertThatThrownBy(() -> useCase.ejecutar(TipoNotificacion.ALERTA_LLUVIA, alta))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("entre 0 y 100");
        assertThatThrownBy(() -> useCase.ejecutar(TipoNotificacion.ALERTA_LLUVIA, baja))
                .isInstanceOf(ValidacionException.class);
    }

    @Test
    void alertaLluviaAceptaLosLimites() {
        when(despachador.despachar(any(), any(), any())).thenReturn(EstadoNotificacion.ENVIADA);
        when(notificacionGateway.guardar(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        for (int limite : new int[]{0, 100}) {
            SolicitudNotificacion s = con(TestData.solicitudCompleta(), null, null, null, limite);
            assertThat(useCase.ejecutar(TipoNotificacion.ALERTA_LLUVIA, s).getEstado())
                    .isEqualTo(EstadoNotificacion.ENVIADA);
        }
    }

    @Test
    void rechazaTipoOSolicitudNulos() {
        assertThatThrownBy(() -> useCase.ejecutar(null, TestData.solicitudCompleta()))
                .isInstanceOf(ValidacionException.class);
        assertThatThrownBy(() -> useCase.ejecutar(TipoNotificacion.CONFIRMACION, null))
                .isInstanceOf(ValidacionException.class);
    }

    @Test
    void recordatorioNoExigeCamposExtra() {
        when(despachador.despachar(any(), any(), any())).thenReturn(EstadoNotificacion.ENVIADA);
        when(notificacionGateway.guardar(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));
        SolicitudNotificacion soloBasicos = con(TestData.solicitudCompleta(), null, null, null, null);

        Notificacion resultado = useCase.ejecutar(TipoNotificacion.RECORDATORIO, soloBasicos);

        assertThat(resultado.getTipo()).isEqualTo(TipoNotificacion.RECORDATORIO);
    }

    /** Copia la solicitud dejando el motivo en null y cambiando solo los demas campos extra. */
    private static SolicitudNotificacion con(SolicitudNotificacion base, Long precio,
                                             LocalDate fechaAnterior, LocalTime horaAnterior, Integer probabilidad) {
        return new SolicitudNotificacion(base.usuarioId(), base.nombreUsuario(), base.canal(), base.destinatario(),
                base.reservaId(), base.nombreCancha(), base.fecha(), base.horaInicio(), base.horaFin(),
                precio, null, fechaAnterior, horaAnterior, probabilidad);
    }
}
