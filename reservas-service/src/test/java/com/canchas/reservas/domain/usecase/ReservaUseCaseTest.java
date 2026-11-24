package com.canchas.reservas.domain.usecase;

import com.canchas.reservas.domain.exception.CodigoError;
import com.canchas.reservas.domain.exception.ReservasException;
import com.canchas.reservas.domain.model.Bloqueo;
import com.canchas.reservas.domain.model.Canal;
import com.canchas.reservas.domain.model.CanchaInfo;
import com.canchas.reservas.domain.model.DatosNotificacion;
import com.canchas.reservas.domain.model.EstadoCancha;
import com.canchas.reservas.domain.model.EstadoReserva;
import com.canchas.reservas.domain.model.HorarioCancha;
import com.canchas.reservas.domain.model.MotivoCancelacion;
import com.canchas.reservas.domain.model.Reserva;
import com.canchas.reservas.domain.model.UsuarioInfo;
import com.canchas.reservas.domain.model.gateway.CanchaServiceGateway;
import com.canchas.reservas.domain.model.gateway.NotificacionServiceGateway;
import com.canchas.reservas.domain.model.gateway.ReservaGateway;
import com.canchas.reservas.domain.model.gateway.UsuarioServiceGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.canchas.reservas.Fixtures.AHORA;
import static com.canchas.reservas.Fixtures.CLOCK;
import static com.canchas.reservas.Fixtures.FUTURO;
import static com.canchas.reservas.Fixtures.HOY;
import static com.canchas.reservas.Fixtures.admin;
import static com.canchas.reservas.Fixtures.assertCodigo;
import static com.canchas.reservas.Fixtures.cancha;
import static com.canchas.reservas.Fixtures.confirmada;
import static com.canchas.reservas.Fixtures.h;
import static com.canchas.reservas.Fixtures.horario;
import static com.canchas.reservas.Fixtures.reserva;
import static com.canchas.reservas.Fixtures.usuario;
import static com.canchas.reservas.Fixtures.usuarioTelegram;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReservaUseCaseTest {

    @Mock
    ReservaGateway reservas;
    @Mock
    UsuarioServiceGateway usuarios;
    @Mock
    CanchaServiceGateway canchas;
    @Mock
    NotificacionServiceGateway notificaciones;

    ReservaUseCase useCase;

    @BeforeEach
    void preparar() {
        useCase = new ReservaUseCase(reservas, usuarios, canchas, new NotificadorReserva(notificaciones),
                new ValidadorAdmin(usuarios), CLOCK);
        when(usuarios.validar(2L)).thenReturn(usuario());
        when(usuarios.validar(3L)).thenReturn(usuarioTelegram());
        when(usuarios.validar(1L)).thenReturn(admin());
        when(canchas.obtenerCancha(1L)).thenReturn(cancha());
        when(canchas.obtenerHorario(eq(1L), any(LocalDate.class))).thenAnswer(inv -> horario(1L, inv.getArgument(1)));
        when(canchas.obtenerPrecioPorHora(eq(1L), any(LocalDate.class))).thenReturn(50_000L);
        when(reservas.existeSolapamiento(anyLong(), any(), any(), any(), any())).thenReturn(false);
        when(reservas.contarConfirmadasFuturas(anyLong(), any())).thenReturn(0L);
        when(reservas.guardar(any(Reserva.class))).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            return r.id() != null ? r : new Reserva(10L, r.canchaId(), r.usuarioId(), r.fecha(), r.horaInicio(),
                    r.horaFin(), r.precioTotal(), r.estado(), r.motivoCancelacion(), r.fechaCreacion());
        });
    }

    // ----------------------------------------------------------------- crear

    @Test
    void crearGuardaReservaConfirmadaConPrecioPorHorasYNotifica() {
        Reserva r = useCase.crear(1L, 2L, FUTURO, h(18), h(20));

        assertEquals(10L, r.id());
        assertEquals(EstadoReserva.CONFIRMADA, r.estado());
        assertEquals(100_000L, r.precioTotal());
        assertNull(r.motivoCancelacion());
        assertEquals(AHORA, r.fechaCreacion());

        ArgumentCaptor<DatosNotificacion> datos = ArgumentCaptor.forClass(DatosNotificacion.class);
        verify(notificaciones).enviarConfirmacion(datos.capture());
        assertEquals(Canal.EMAIL, datos.getValue().canal());
        assertEquals("usuario@mail.com", datos.getValue().destinatario());
        assertEquals("Sintética 1", datos.getValue().nombreCancha());
        assertEquals(10L, datos.getValue().reservaId());
    }

    @Test
    void crearUsaTelegramSiElUsuarioTieneChatId() {
        useCase.crear(1L, 3L, FUTURO, h(10), h(11));

        ArgumentCaptor<DatosNotificacion> datos = ArgumentCaptor.forClass(DatosNotificacion.class);
        verify(notificaciones).enviarConfirmacion(datos.capture());
        assertEquals(Canal.TELEGRAM, datos.getValue().canal());
        assertEquals("123456789", datos.getValue().destinatario());
    }

    @Test
    void crearConDatosFaltantesDevuelveValidacion() {
        assertCodigo(CodigoError.VALIDACION, () -> useCase.crear(null, 2L, FUTURO, h(18), h(19)));
        assertCodigo(CodigoError.VALIDACION, () -> useCase.crear(1L, 2L, FUTURO, null, h(19)));
    }

    @Test
    void crearValidaFormatoAntesDeLlamarOtrosServicios() {
        assertCodigo(CodigoError.DURACION_INVALIDA, () -> useCase.crear(1L, 2L, FUTURO, h(18).withMinute(30), h(20)));
        assertCodigo(CodigoError.DURACION_INVALIDA, () -> useCase.crear(1L, 2L, FUTURO, h(18), h(20).withMinute(15)));
        assertCodigo(CodigoError.DURACION_INVALIDA, () -> useCase.crear(1L, 2L, FUTURO, h(19), h(19)));
        assertCodigo(CodigoError.DURACION_INVALIDA, () -> useCase.crear(1L, 2L, FUTURO, h(19), h(18)));
        assertCodigo(CodigoError.DURACION_INVALIDA, () -> useCase.crear(1L, 2L, FUTURO, h(10), h(14)));
        verifyNoInteractions(usuarios, canchas, reservas);
    }

    @Test
    void crearAceptaReservasDeTresHoras() {
        assertEquals(150_000L, useCase.crear(1L, 2L, FUTURO, h(10), h(13)).precioTotal());
    }

    @Test
    void crearRechazaFechasPasadasYMuyLejanas() {
        assertCodigo(CodigoError.FECHA_INVALIDA, () -> useCase.crear(1L, 2L, HOY.minusDays(1), h(18), h(19)));
        // hoy, pero la franja ya empezó (ahora son las 10:00)
        assertCodigo(CodigoError.FECHA_INVALIDA, () -> useCase.crear(1L, 2L, HOY, h(9), h(10)));
        assertCodigo(CodigoError.FECHA_INVALIDA, () -> useCase.crear(1L, 2L, HOY.plusDays(31), h(18), h(19)));
        verifyNoInteractions(usuarios);
    }

    @Test
    void crearPermiteHoyEnUnaFranjaFuturaYHastaTreintaDias() {
        assertDoesNotThrow(() -> useCase.crear(1L, 2L, HOY, h(14), h(15)));
        assertDoesNotThrow(() -> useCase.crear(1L, 2L, HOY.plusDays(30), h(14), h(15)));
    }

    @Test
    void crearPropagaUsuarioInexistente() {
        when(usuarios.validar(99L)).thenThrow(new ReservasException(CodigoError.USUARIO_NO_ENCONTRADO, "no existe"));
        assertCodigo(CodigoError.USUARIO_NO_ENCONTRADO, () -> useCase.crear(1L, 99L, FUTURO, h(18), h(19)));
        verifyNoInteractions(canchas);
    }

    @Test
    void crearRechazaUsuarioInactivo() {
        when(usuarios.validar(4L)).thenReturn(new UsuarioInfo(4L, "Inactivo", "i@mail.com",
                com.canchas.reservas.domain.model.Rol.USUARIO, false, null));
        assertCodigo(CodigoError.USUARIO_INACTIVO, () -> useCase.crear(1L, 4L, FUTURO, h(18), h(19)));
    }

    @Test
    void crearPropagaFallosDeOtrosServicios() {
        when(canchas.obtenerCancha(99L)).thenThrow(new ReservasException(CodigoError.CANCHA_NO_ENCONTRADA, "no existe"));
        assertCodigo(CodigoError.CANCHA_NO_ENCONTRADA, () -> useCase.crear(99L, 2L, FUTURO, h(18), h(19)));

        when(usuarios.validar(5L)).thenThrow(new ReservasException(CodigoError.SERVICIO_NO_DISPONIBLE, "caído"));
        assertCodigo(CodigoError.SERVICIO_NO_DISPONIBLE, () -> useCase.crear(1L, 5L, FUTURO, h(18), h(19)));
    }

    @Test
    void crearRechazaCanchaEnMantenimientoOInactiva() {
        when(canchas.obtenerCancha(3L)).thenReturn(new CanchaInfo(3L, "Pádel", 4.0, -74.0, false,
                EstadoCancha.MANTENIMIENTO));
        assertCodigo(CodigoError.CANCHA_NO_DISPONIBLE, () -> useCase.crear(3L, 2L, FUTURO, h(18), h(19)));
    }

    @Test
    void crearRechazaFranjaFueraDelHorario() {
        assertCodigo(CodigoError.FUERA_DE_HORARIO, () -> useCase.crear(1L, 2L, FUTURO, h(5), h(6)));
        assertCodigo(CodigoError.FUERA_DE_HORARIO, () -> useCase.crear(1L, 2L, FUTURO, h(22), h(23)));
        assertDoesNotThrow(() -> useCase.crear(1L, 2L, FUTURO, h(21), h(22)));
    }

    @Test
    void crearRechazaFranjaQueChocaConBloqueoPeroNoLaAdyacente() {
        when(canchas.obtenerHorario(eq(1L), any(LocalDate.class))).thenAnswer(inv -> new HorarioCancha(1L,
                inv.getArgument(1), EstadoCancha.ACTIVA, h(6), h(22), List.of(new Bloqueo(h(14), h(16), "Mant"))));
        assertCodigo(CodigoError.CANCHA_NO_DISPONIBLE, () -> useCase.crear(1L, 2L, FUTURO, h(15), h(17)));
        assertDoesNotThrow(() -> useCase.crear(1L, 2L, FUTURO, h(16), h(17)));
    }

    @Test
    void crearRechazaFranjaOcupada() {
        when(reservas.existeSolapamiento(1L, FUTURO, h(18), h(19), null)).thenReturn(true);
        assertCodigo(CodigoError.FRANJA_OCUPADA, () -> useCase.crear(1L, 2L, FUTURO, h(18), h(19)));
        verify(reservas, never()).guardar(any());
    }

    @Test
    void crearRechazaSiElUsuarioYaTieneTresReservasFuturas() {
        when(reservas.contarConfirmadasFuturas(eq(2L), any())).thenReturn(3L);
        assertCodigo(CodigoError.LIMITE_RESERVAS, () -> useCase.crear(1L, 2L, FUTURO, h(18), h(19)));
        when(reservas.contarConfirmadasFuturas(eq(2L), any())).thenReturn(2L);
        assertDoesNotThrow(() -> useCase.crear(1L, 2L, FUTURO, h(18), h(19)));
    }

    @Test
    void crearNoFallaSiLaNotificacionLanzaExcepcion() {
        doThrow(new RuntimeException("telegram caído")).when(notificaciones).enviarConfirmacion(any());
        Reserva r = useCase.crear(1L, 2L, FUTURO, h(18), h(19));
        assertEquals(EstadoReserva.CONFIRMADA, r.estado());
    }

    // ------------------------------------------------------------ consultas

    @Test
    void obtenerDevuelveLaReservaOLanzaNoEncontrada() {
        when(reservas.buscarPorId(1L)).thenReturn(Optional.of(confirmada(1L)));
        when(reservas.buscarPorId(9L)).thenReturn(Optional.empty());

        assertEquals(1L, useCase.obtener(1L).id());
        assertCodigo(CodigoError.RESERVA_NO_ENCONTRADA, () -> useCase.obtener(9L));
    }

    @Test
    void eliminarBorraSoloSiExiste() {
        when(reservas.buscarPorId(1L)).thenReturn(Optional.of(confirmada(1L)));
        when(reservas.buscarPorId(9L)).thenReturn(Optional.empty());

        useCase.eliminar(1L);
        verify(reservas).eliminar(1L);

        assertCodigo(CodigoError.RESERVA_NO_ENCONTRADA, () -> useCase.eliminar(9L));
        verify(reservas, never()).eliminar(9L);
    }

    @Test
    void listarHistorialYAgendaDelegan() {
        List<Reserva> lista = List.of(confirmada(1L));
        when(reservas.listarTodas()).thenReturn(lista);
        when(reservas.buscarPorUsuario(2L)).thenReturn(lista);
        when(reservas.buscarConfirmadasPorCanchaYFecha(1L, FUTURO)).thenReturn(lista);

        assertEquals(lista, useCase.listarTodas());
        assertEquals(lista, useCase.historialDeUsuario(2L));
        assertEquals(lista, useCase.agendaDeCancha(1L, FUTURO));
        assertCodigo(CodigoError.VALIDACION, () -> useCase.agendaDeCancha(null, FUTURO));
    }

    // -------------------------------------------------------------- cancelar

    @Test
    void cancelarSinMotivoUsaUsuarioYNotifica() {
        when(reservas.buscarPorId(1L)).thenReturn(Optional.of(confirmada(1L)));

        Reserva r = useCase.cancelar(1L, null);

        assertEquals(EstadoReserva.CANCELADA, r.estado());
        assertEquals(MotivoCancelacion.USUARIO, r.motivoCancelacion());
        verify(notificaciones).enviarCancelacion(any(DatosNotificacion.class), eq(MotivoCancelacion.USUARIO));
    }

    @Test
    void cancelarSoloReservasConfirmadas() {
        when(reservas.buscarPorId(1L)).thenReturn(Optional.of(reserva(1L, FUTURO, 18, 19, EstadoReserva.CANCELADA)));
        when(reservas.buscarPorId(9L)).thenReturn(Optional.empty());

        assertCodigo(CodigoError.RESERVA_NO_CANCELABLE, () -> useCase.cancelar(1L, MotivoCancelacion.USUARIO));
        assertCodigo(CodigoError.RESERVA_NO_ENCONTRADA, () -> useCase.cancelar(9L, MotivoCancelacion.USUARIO));
    }

    @Test
    void cancelarExigeDosHorasDeAnticipacionSalvoLluvia() {
        // ahora son las 10:00: una reserva a las 11:00 está a 1 hora
        Reserva enUnaHora = reserva(1L, HOY, 11, 12, EstadoReserva.CONFIRMADA);
        when(reservas.buscarPorId(1L)).thenReturn(Optional.of(enUnaHora));

        assertCodigo(CodigoError.CANCELACION_TARDIA, () -> useCase.cancelar(1L, MotivoCancelacion.USUARIO));
        assertCodigo(CodigoError.CANCELACION_TARDIA, () -> useCase.cancelar(1L, MotivoCancelacion.ADMIN));
        assertEquals(EstadoReserva.CANCELADA, useCase.cancelar(1L, MotivoCancelacion.LLUVIA).estado());
    }

    @Test
    void cancelarConExactamenteDosHorasEsPermitido() {
        when(reservas.buscarPorId(1L)).thenReturn(Optional.of(reserva(1L, HOY, 12, 13, EstadoReserva.CONFIRMADA)));
        assertEquals(EstadoReserva.CANCELADA, useCase.cancelar(1L, MotivoCancelacion.USUARIO).estado());
    }

    @Test
    void cancelarSigueAdelanteSiNoSePuedeArmarLaNotificacion() {
        when(reservas.buscarPorId(1L)).thenReturn(Optional.of(confirmada(1L)));
        when(usuarios.validar(2L)).thenThrow(new ReservasException(CodigoError.SERVICIO_NO_DISPONIBLE, "caído"));

        assertEquals(EstadoReserva.CANCELADA, useCase.cancelar(1L, MotivoCancelacion.USUARIO).estado());
        verify(notificaciones, never()).enviarCancelacion(any(), any());
    }

    // ----------------------------------------------------------- reprogramar

    @Test
    void reprogramarRecalculaPrecioYNotificaConLaFranjaAnterior() {
        when(reservas.buscarPorId(1L)).thenReturn(Optional.of(confirmada(1L)));
        LocalDate nueva = FUTURO.plusDays(2);

        Reserva r = useCase.reprogramar(1L, nueva, h(10), h(11));

        assertEquals(nueva, r.fecha());
        assertEquals(h(10), r.horaInicio());
        assertEquals(50_000L, r.precioTotal());
        verify(reservas).existeSolapamiento(1L, nueva, h(10), h(11), 1L); // se excluye a sí misma
        verify(notificaciones).enviarReprogramacion(any(DatosNotificacion.class), eq(FUTURO), eq(h(18)));
    }

    @Test
    void reprogramarAplicaLasReglasDeCrearSobreLaNuevaFranja() {
        when(reservas.buscarPorId(1L)).thenReturn(Optional.of(confirmada(1L)));

        assertCodigo(CodigoError.DURACION_INVALIDA, () -> useCase.reprogramar(1L, FUTURO, h(10), h(10)));
        assertCodigo(CodigoError.FECHA_INVALIDA, () -> useCase.reprogramar(1L, HOY.minusDays(1), h(10), h(11)));
        assertCodigo(CodigoError.FUERA_DE_HORARIO, () -> useCase.reprogramar(1L, FUTURO, h(4), h(5)));

        when(reservas.existeSolapamiento(1L, FUTURO, h(10), h(11), 1L)).thenReturn(true);
        assertCodigo(CodigoError.FRANJA_OCUPADA, () -> useCase.reprogramar(1L, FUTURO, h(10), h(11)));
        verify(reservas, never()).guardar(any());
    }

    @Test
    void reprogramarSoloReservasConfirmadasExistentesYCompletas() {
        when(reservas.buscarPorId(1L)).thenReturn(Optional.of(reserva(1L, FUTURO, 18, 19, EstadoReserva.CANCELADA)));
        when(reservas.buscarPorId(9L)).thenReturn(Optional.empty());

        assertCodigo(CodigoError.ESTADO_RESERVA_INVALIDO, () -> useCase.reprogramar(1L, FUTURO, h(10), h(11)));
        assertCodigo(CodigoError.RESERVA_NO_ENCONTRADA, () -> useCase.reprogramar(9L, FUTURO, h(10), h(11)));
        assertCodigo(CodigoError.VALIDACION, () -> useCase.reprogramar(1L, null, h(10), h(11)));
    }

    // ------------------------------------------------------------ asistencia

    @Test
    void asistenciaMarcaCompletadaONoAsistioEnReservasYaIniciadas() {
        when(reservas.buscarPorId(1L)).thenReturn(Optional.of(reserva(1L, HOY, 8, 9, EstadoReserva.CONFIRMADA)));

        assertEquals(EstadoReserva.COMPLETADA, useCase.registrarAsistencia(1L, 1L, EstadoReserva.COMPLETADA).estado());
        assertEquals(EstadoReserva.NO_ASISTIO, useCase.registrarAsistencia(1L, 1L, EstadoReserva.NO_ASISTIO).estado());
    }

    @Test
    void asistenciaExigeAdminYEstadoValido() {
        assertCodigo(CodigoError.SIN_CREDENCIALES, () -> useCase.registrarAsistencia(null, 1L, EstadoReserva.COMPLETADA));
        assertCodigo(CodigoError.PERMISO_DENEGADO, () -> useCase.registrarAsistencia(2L, 1L, EstadoReserva.COMPLETADA));
        assertCodigo(CodigoError.VALIDACION, () -> useCase.registrarAsistencia(1L, 1L, EstadoReserva.CANCELADA));
        assertCodigo(CodigoError.VALIDACION, () -> useCase.registrarAsistencia(1L, 1L, null));
    }

    @Test
    void asistenciaNoAplicaSiNoEstaConfirmadaOAunNoEmpieza() {
        when(reservas.buscarPorId(1L)).thenReturn(Optional.of(reserva(1L, HOY, 8, 9, EstadoReserva.COMPLETADA)));
        when(reservas.buscarPorId(2L)).thenReturn(Optional.of(confirmada(2L)));
        when(reservas.buscarPorId(9L)).thenReturn(Optional.empty());

        assertCodigo(CodigoError.ESTADO_RESERVA_INVALIDO, () -> useCase.registrarAsistencia(1L, 1L, EstadoReserva.COMPLETADA));
        assertCodigo(CodigoError.ESTADO_RESERVA_INVALIDO, () -> useCase.registrarAsistencia(1L, 2L, EstadoReserva.COMPLETADA));
        assertCodigo(CodigoError.RESERVA_NO_ENCONTRADA, () -> useCase.registrarAsistencia(1L, 9L, EstadoReserva.COMPLETADA));
    }

    @Test
    void elCandadoNoImpideReservarFranjasDistintasEnParalelo() throws Exception {
        var pool = java.util.concurrent.Executors.newFixedThreadPool(4);
        try {
            var tareas = new java.util.ArrayList<java.util.concurrent.Future<Reserva>>();
            for (int hora = 6; hora < 10; hora++) {
                int inicio = hora;
                tareas.add(pool.submit(() -> useCase.crear(1L, 2L, FUTURO, h(inicio), h(inicio + 1))));
            }
            for (var tarea : tareas) {
                assertTrue(tarea.get().id() != null);
            }
        } finally {
            pool.shutdown();
        }
    }
}
