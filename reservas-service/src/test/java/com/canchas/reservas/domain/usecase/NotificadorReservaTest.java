package com.canchas.reservas.domain.usecase;

import com.canchas.reservas.domain.model.Canal;
import com.canchas.reservas.domain.model.DatosNotificacion;
import com.canchas.reservas.domain.model.EstadoReserva;
import com.canchas.reservas.domain.model.MotivoCancelacion;
import com.canchas.reservas.domain.model.Reserva;
import com.canchas.reservas.domain.model.Rol;
import com.canchas.reservas.domain.model.UsuarioInfo;
import com.canchas.reservas.domain.model.gateway.NotificacionServiceGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static com.canchas.reservas.Fixtures.FUTURO;
import static com.canchas.reservas.Fixtures.cancha;
import static com.canchas.reservas.Fixtures.h;
import static com.canchas.reservas.Fixtures.reserva;
import static com.canchas.reservas.Fixtures.usuario;
import static com.canchas.reservas.Fixtures.usuarioTelegram;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificadorReservaTest {

    @Mock
    NotificacionServiceGateway gateway;

    NotificadorReserva notificador;
    Reserva reserva;

    @BeforeEach
    void preparar() {
        notificador = new NotificadorReserva(gateway);
        reserva = reserva(55L, FUTURO, 18, 19, EstadoReserva.CONFIRMADA);
    }

    @Test
    void armaLosDatosComunesDelContrato() {
        DatosNotificacion d = NotificadorReserva.datos(reserva, usuarioTelegram(), cancha());

        assertEquals(3L, d.usuarioId());
        assertEquals("Ana Pérez", d.nombreUsuario());
        assertEquals(Canal.TELEGRAM, d.canal());
        assertEquals("123456789", d.destinatario());
        assertEquals(55L, d.reservaId());
        assertEquals("Sintética 1", d.nombreCancha());
        assertEquals(FUTURO, d.fecha());
        assertEquals(h(18), d.horaInicio());
        assertEquals(h(19), d.horaFin());
        assertEquals(50_000L, d.precioTotal());
    }

    @Test
    void sinTelegramUsaCorreo() {
        DatosNotificacion d = NotificadorReserva.datos(reserva, usuario(), cancha());
        assertEquals(Canal.EMAIL, d.canal());
        assertEquals("usuario@mail.com", d.destinatario());
    }

    @Test
    void telegramEnBlancoSeTrataComoSinTelegram() {
        UsuarioInfo conBlanco = new UsuarioInfo(2L, "U", "u@mail.com", Rol.USUARIO, true, "  ");
        assertEquals(Canal.EMAIL, NotificadorReserva.datos(reserva, conBlanco, cancha()).canal());
    }

    @Test
    void cadaTipoLlamaAlGatewayCorrespondiente() {
        notificador.confirmacion(reserva, usuario(), cancha());
        notificador.recordatorio(reserva, usuario(), cancha());
        notificador.cancelacion(reserva, usuario(), cancha(), MotivoCancelacion.LLUVIA);
        notificador.reprogramacion(reserva, usuario(), cancha(), FUTURO.minusDays(1), h(9));
        notificador.alertaLluvia(reserva, usuario(), cancha(), 75);

        verify(gateway).enviarConfirmacion(any(DatosNotificacion.class));
        verify(gateway).enviarRecordatorio(any(DatosNotificacion.class));
        verify(gateway).enviarCancelacion(any(DatosNotificacion.class), eq(MotivoCancelacion.LLUVIA));
        verify(gateway).enviarReprogramacion(any(DatosNotificacion.class), eq(FUTURO.minusDays(1)), eq(h(9)));
        ArgumentCaptor<Integer> prob = ArgumentCaptor.forClass(Integer.class);
        verify(gateway).enviarAlertaLluvia(any(DatosNotificacion.class), prob.capture());
        assertEquals(75, prob.getValue());
    }

    @Test
    void ningunFalloDelGatewayEscapa() {
        doThrow(new RuntimeException("x")).when(gateway).enviarConfirmacion(any());
        doThrow(new RuntimeException("x")).when(gateway).enviarRecordatorio(any());
        doThrow(new RuntimeException("x")).when(gateway).enviarCancelacion(any(), any());
        doThrow(new RuntimeException("x")).when(gateway).enviarReprogramacion(any(), any(), any());
        doThrow(new RuntimeException("x")).when(gateway).enviarAlertaLluvia(any(), any(Integer.class));

        assertDoesNotThrow(() -> {
            notificador.confirmacion(reserva, usuario(), cancha());
            notificador.recordatorio(reserva, usuario(), cancha());
            notificador.cancelacion(reserva, usuario(), cancha(), MotivoCancelacion.USUARIO);
            notificador.reprogramacion(reserva, usuario(), cancha(), FUTURO, h(9));
            notificador.alertaLluvia(reserva, usuario(), cancha(), 90);
        });
    }
}
