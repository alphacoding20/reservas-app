package com.canchas.reservas.domain.usecase;

import com.canchas.reservas.domain.exception.CodigoError;
import com.canchas.reservas.domain.exception.ReservasException;
import com.canchas.reservas.domain.model.CanchaInfo;
import com.canchas.reservas.domain.model.DatosNotificacion;
import com.canchas.reservas.domain.model.EstadoCancha;
import com.canchas.reservas.domain.model.EstadoReserva;
import com.canchas.reservas.domain.model.Pronostico;
import com.canchas.reservas.domain.model.Reserva;
import com.canchas.reservas.domain.model.ResumenAlertaLluvia;
import com.canchas.reservas.domain.model.gateway.CanchaServiceGateway;
import com.canchas.reservas.domain.model.gateway.ClimaGateway;
import com.canchas.reservas.domain.model.gateway.NotificacionServiceGateway;
import com.canchas.reservas.domain.model.gateway.ReservaGateway;
import com.canchas.reservas.domain.model.gateway.UsuarioServiceGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.canchas.reservas.Fixtures.AHORA;
import static com.canchas.reservas.Fixtures.FUTURO;
import static com.canchas.reservas.Fixtures.admin;
import static com.canchas.reservas.Fixtures.assertCodigo;
import static com.canchas.reservas.Fixtures.cancha;
import static com.canchas.reservas.Fixtures.canchaTechada;
import static com.canchas.reservas.Fixtures.h;
import static com.canchas.reservas.Fixtures.reserva;
import static com.canchas.reservas.Fixtures.usuario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PronosticoUseCaseTest {

    @Mock
    ReservaGateway reservas;
    @Mock
    UsuarioServiceGateway usuarios;
    @Mock
    CanchaServiceGateway canchas;
    @Mock
    ClimaGateway clima;
    @Mock
    NotificacionServiceGateway notificaciones;

    PronosticoUseCase useCase;

    @BeforeEach
    void preparar() {
        useCase = new PronosticoUseCase(reservas, usuarios, canchas, clima, new NotificadorReserva(notificaciones),
                new ValidadorAdmin(usuarios), 60);
        when(usuarios.validar(1L)).thenReturn(admin());
        when(usuarios.validar(2L)).thenReturn(usuario());
        when(canchas.obtenerCancha(1L)).thenReturn(cancha());
        when(canchas.obtenerCancha(2L)).thenReturn(canchaTechada());
        when(reservas.buscarPorId(1L)).thenReturn(Optional.of(reserva(1L, FUTURO, 18, 19, EstadoReserva.CONFIRMADA)));
    }

    private void climaDevuelve(Optional<Integer> probabilidad) {
        when(clima.consultarProbabilidadLluvia(anyDouble(), anyDouble(), any(LocalDate.class), any(), any()))
                .thenReturn(probabilidad);
    }

    // ----------------------------------------------------------- pronóstico

    @Test
    void marcaRiesgoCuandoLaProbabilidadSuperaElUmbral() {
        climaDevuelve(Optional.of(80));

        Pronostico p = useCase.pronosticoDeReserva(1L);

        assertTrue(p.aplica());
        assertTrue(p.pronosticoDisponible());
        assertEquals(80, p.probabilidadLluvia());
        assertTrue(p.riesgoLluvia());
        assertEquals(60, p.umbral());
        assertEquals(1L, p.reservaId());
    }

    @Test
    void elUmbralEsInclusivo() {
        climaDevuelve(Optional.of(60));
        assertTrue(useCase.pronosticoDeReserva(1L).riesgoLluvia());
        climaDevuelve(Optional.of(59));
        assertFalse(useCase.pronosticoDeReserva(1L).riesgoLluvia());
    }

    @Test
    void sinPronosticoDisponibleNoEsErrorYNoHayRiesgo() {
        climaDevuelve(Optional.empty());

        Pronostico p = useCase.pronosticoDeReserva(1L);

        assertTrue(p.aplica());
        assertFalse(p.pronosticoDisponible());
        assertNull(p.probabilidadLluvia());
        assertFalse(p.riesgoLluvia());
    }

    @Test
    void canchaTechadaNoAplicaYNoConsultaElClima() {
        when(reservas.buscarPorId(5L)).thenReturn(Optional.of(
                new Reserva(5L, 2L, 2L, FUTURO, h(18), h(19), 40_000L, EstadoReserva.CONFIRMADA, null, AHORA)));

        Pronostico p = useCase.pronosticoDeReserva(5L);

        assertFalse(p.aplica());
        assertFalse(p.riesgoLluvia());
        assertNull(p.probabilidadLluvia());
        verifyNoInteractions(clima);
    }

    @Test
    void canchaSinCoordenadasNoTienePronostico() {
        when(canchas.obtenerCancha(1L)).thenReturn(new CanchaInfo(1L, "Sin GPS", null, null, false,
                EstadoCancha.ACTIVA));

        assertFalse(useCase.pronosticoDeReserva(1L).pronosticoDisponible());
        verifyNoInteractions(clima);
    }

    @Test
    void reservaInexistenteDevuelveNoEncontrada() {
        when(reservas.buscarPorId(9L)).thenReturn(Optional.empty());
        assertCodigo(CodigoError.RESERVA_NO_ENCONTRADA, () -> useCase.pronosticoDeReserva(9L));
    }

    // --------------------------------------------------------- alerta lluvia

    @Test
    void alertaSoloNotificaReservasEnCanchasAbiertasConRiesgo() {
        Reserva conLluvia = reserva(1L, FUTURO, 18, 19, EstadoReserva.CONFIRMADA);
        Reserva sinDatos = reserva(3L, FUTURO, 10, 11, EstadoReserva.CONFIRMADA);
        Reserva techada = new Reserva(2L, 2L, 2L, FUTURO, h(18), h(19), 40_000L, EstadoReserva.CONFIRMADA, null,
                AHORA);
        when(reservas.buscarConfirmadasPorFecha(FUTURO)).thenReturn(List.of(conLluvia, techada, sinDatos));
        when(clima.consultarProbabilidadLluvia(anyDouble(), anyDouble(), eq(FUTURO), eq(h(18)), eq(h(19))))
                .thenReturn(Optional.of(80));
        when(clima.consultarProbabilidadLluvia(anyDouble(), anyDouble(), eq(FUTURO), eq(h(10)), eq(h(11))))
                .thenReturn(Optional.empty());

        ResumenAlertaLluvia resumen = useCase.alertarLluvia(1L, FUTURO);

        assertEquals(2, resumen.reservasRevisadas()); // la techada no se revisa
        assertEquals(1, resumen.alertasEnviadas());
        assertEquals(1, resumen.sinPronostico());
        assertEquals(FUTURO, resumen.fecha());
        verify(notificaciones, times(1)).enviarAlertaLluvia(any(DatosNotificacion.class), eq(80));
    }

    @Test
    void alertaSinRiesgoNoEnviaNada() {
        when(reservas.buscarConfirmadasPorFecha(FUTURO))
                .thenReturn(List.of(reserva(1L, FUTURO, 18, 19, EstadoReserva.CONFIRMADA)));
        climaDevuelve(Optional.of(10));

        ResumenAlertaLluvia resumen = useCase.alertarLluvia(1L, FUTURO);

        assertEquals(1, resumen.reservasRevisadas());
        assertEquals(0, resumen.alertasEnviadas());
        verify(notificaciones, never()).enviarAlertaLluvia(any(), any(Integer.class));
    }

    @Test
    void alertaCuentaComoSinPronosticoLaCanchaQueNoSePudoConsultar() {
        Reserva enCanchaCaida = new Reserva(7L, 9L, 2L, FUTURO, h(18), h(19), 1L, EstadoReserva.CONFIRMADA, null, AHORA);
        when(reservas.buscarConfirmadasPorFecha(FUTURO)).thenReturn(List.of(enCanchaCaida));
        when(canchas.obtenerCancha(9L)).thenThrow(new ReservasException(CodigoError.SERVICIO_NO_DISPONIBLE, "caído"));

        ResumenAlertaLluvia resumen = useCase.alertarLluvia(1L, FUTURO);

        assertEquals(1, resumen.reservasRevisadas());
        assertEquals(1, resumen.sinPronostico());
        assertEquals(0, resumen.alertasEnviadas());
    }

    @Test
    void alertaNoSeCuentaSiNoSePuedeObtenerElUsuario() {
        when(reservas.buscarConfirmadasPorFecha(FUTURO))
                .thenReturn(List.of(reserva(1L, FUTURO, 18, 19, EstadoReserva.CONFIRMADA)));
        climaDevuelve(Optional.of(95));
        when(usuarios.validar(2L)).thenThrow(new ReservasException(CodigoError.USUARIO_NO_ENCONTRADO, "no existe"));

        assertEquals(0, useCase.alertarLluvia(1L, FUTURO).alertasEnviadas());
        verify(notificaciones, never()).enviarAlertaLluvia(any(), any(Integer.class));
    }

    @Test
    void alertaExigeAdminYFecha() {
        assertCodigo(CodigoError.SIN_CREDENCIALES, () -> useCase.alertarLluvia(null, FUTURO));
        assertCodigo(CodigoError.PERMISO_DENEGADO, () -> useCase.alertarLluvia(2L, FUTURO));
        assertCodigo(CodigoError.VALIDACION, () -> useCase.alertarLluvia(1L, null));
        verify(reservas, never()).buscarConfirmadasPorFecha(any());
    }
}
