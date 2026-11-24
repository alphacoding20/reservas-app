package com.canchas.reservas.domain.model;

import com.canchas.reservas.domain.exception.CodigoError;
import com.canchas.reservas.domain.exception.ReservasException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.canchas.reservas.Fixtures.AHORA;
import static com.canchas.reservas.Fixtures.FUTURO;
import static com.canchas.reservas.Fixtures.h;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModeloDominioTest {

    private final Reserva base = new Reserva(1L, 1L, 2L, FUTURO, h(18), h(20), 100_000L, EstadoReserva.CONFIRMADA,
            null, AHORA);

    @Test
    void franjasChocanSoloSiSeSolapanDeVerdad() {
        assertTrue(Franja.chocan(h(10), h(12), h(11), h(13)));
        assertTrue(Franja.chocan(h(10), h(14), h(11), h(12)));
        assertFalse(Franja.chocan(h(10), h(12), h(12), h(13))); // adyacentes no chocan
        assertFalse(Franja.chocan(h(10), h(11), h(12), h(13)));
    }

    @Test
    void reservaCalculaHorasInicioYChoque() {
        assertEquals(2, base.horas());
        assertEquals(LocalDateTime.of(2026, 12, 1, 18, 0), base.inicio());
        assertTrue(base.chocaCon(h(19), h(21)));
        assertFalse(base.chocaCon(h(20), h(21)));
    }

    @Test
    void reservaEsInmutableYCadaCambioDevuelveUnaCopia() {
        Reserva cancelada = base.cancelada(MotivoCancelacion.LLUVIA);
        assertEquals(EstadoReserva.CANCELADA, cancelada.estado());
        assertEquals(MotivoCancelacion.LLUVIA, cancelada.motivoCancelacion());
        assertEquals(EstadoReserva.CONFIRMADA, base.estado());

        assertEquals(EstadoReserva.COMPLETADA, base.conEstado(EstadoReserva.COMPLETADA).estado());

        Reserva movida = base.reprogramada(FUTURO.plusDays(1), h(9), h(10), 50_000L);
        assertEquals(FUTURO.plusDays(1), movida.fecha());
        assertEquals(h(9), movida.horaInicio());
        assertEquals(50_000L, movida.precioTotal());
        assertEquals(1L, movida.id());
        assertEquals(FUTURO, base.fecha());
    }

    @Test
    void horarioSabeSiContieneUnaFranjaYSiChocaConBloqueos() {
        HorarioCancha horario = new HorarioCancha(1L, FUTURO, EstadoCancha.ACTIVA, h(6), h(22),
                List.of(new Bloqueo(h(14), h(16), "Mantenimiento")));

        assertTrue(horario.contiene(h(6), h(7)));
        assertTrue(horario.contiene(h(21), h(22)));
        assertFalse(horario.contiene(h(5), h(6)));
        assertFalse(horario.contiene(h(22), h(23)));
        assertTrue(horario.chocaConBloqueo(h(15), h(17)));
        assertFalse(horario.chocaConBloqueo(h(16), h(17)));
    }

    @Test
    void horarioSinBloqueosNuncaChoca() {
        HorarioCancha horario = new HorarioCancha(1L, FUTURO, EstadoCancha.ACTIVA, h(6), h(22), null);
        assertTrue(horario.bloqueos().isEmpty());
        assertFalse(horario.chocaConBloqueo(h(10), h(11)));
    }

    @Test
    void usuarioYCanchaExponenSusAyudantes() {
        assertTrue(new UsuarioInfo(1L, "A", "a@m.com", Rol.ADMIN, true, null).esAdmin());
        assertFalse(new UsuarioInfo(1L, "A", "a@m.com", Rol.USUARIO, true, null).esAdmin());
        assertTrue(new UsuarioInfo(1L, "A", "a@m.com", Rol.USUARIO, true, "123").tieneTelegram());
        assertFalse(new UsuarioInfo(1L, "A", "a@m.com", Rol.USUARIO, true, null).tieneTelegram());
        assertTrue(new CanchaInfo(1L, "C", 4.0, -74.0, false, EstadoCancha.ACTIVA).tieneCoordenadas());
        assertFalse(new CanchaInfo(1L, "C", null, -74.0, false, EstadoCancha.ACTIVA).tieneCoordenadas());
        assertFalse(new CanchaInfo(1L, "C", 4.0, null, false, EstadoCancha.ACTIVA).tieneCoordenadas());
    }

    @Test
    void losCodigosDeErrorCumplenElContrato() {
        assertEquals("FRANJA_OCUPADA", CodigoError.FRANJA_OCUPADA.getCodigo());
        assertEquals(409, CodigoError.FRANJA_OCUPADA.getStatus());
        // 401 y 403 comparten el código NO_AUTORIZADO
        assertEquals("NO_AUTORIZADO", CodigoError.SIN_CREDENCIALES.getCodigo());
        assertEquals(401, CodigoError.SIN_CREDENCIALES.getStatus());
        assertEquals("NO_AUTORIZADO", CodigoError.PERMISO_DENEGADO.getCodigo());
        assertEquals(403, CodigoError.PERMISO_DENEGADO.getStatus());
        assertEquals(403, CodigoError.USUARIO_INACTIVO.getStatus());
        assertEquals(502, CodigoError.SERVICIO_NO_DISPONIBLE.getStatus());
        assertEquals(404, CodigoError.RESERVA_NO_ENCONTRADA.getStatus());
        assertEquals(400, CodigoError.DURACION_INVALIDA.getStatus());
    }

    @Test
    void laExcepcionDeDominioGuardaCodigoYMensaje() {
        ReservasException e = new ReservasException(CodigoError.LIMITE_RESERVAS, "demasiadas");
        assertSame(CodigoError.LIMITE_RESERVAS, e.getCodigo());
        assertEquals("demasiadas", e.getMessage());
    }
}
