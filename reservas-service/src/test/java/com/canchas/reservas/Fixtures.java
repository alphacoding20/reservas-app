package com.canchas.reservas;

import com.canchas.reservas.domain.exception.CodigoError;
import com.canchas.reservas.domain.exception.ReservasException;
import com.canchas.reservas.domain.model.CanchaInfo;
import com.canchas.reservas.domain.model.EstadoCancha;
import com.canchas.reservas.domain.model.EstadoReserva;
import com.canchas.reservas.domain.model.HorarioCancha;
import com.canchas.reservas.domain.model.Reserva;
import com.canchas.reservas.domain.model.Rol;
import com.canchas.reservas.domain.model.UsuarioInfo;
import org.junit.jupiter.api.function.Executable;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class Fixtures {

    public static final ZoneId ZONA = ZoneId.of("America/Bogota");
    public static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-23T15:00:00Z"), ZONA);
    public static final LocalDate HOY = LocalDate.of(2026, 11, 23);
    public static final LocalDate FUTURO = LocalDate.of(2026, 12, 1);
    public static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 23, 10, 0);

    private Fixtures() {
    }

    public static LocalTime h(int hora) {
        return LocalTime.of(hora, 0);
    }

    public static UsuarioInfo usuario() {
        return new UsuarioInfo(2L, "Usuario de prueba", "usuario@mail.com", Rol.USUARIO, true, null);
    }

    public static UsuarioInfo usuarioTelegram() {
        return new UsuarioInfo(3L, "Ana Pérez", "ana@mail.com", Rol.USUARIO, true, "123456789");
    }

    public static UsuarioInfo admin() {
        return new UsuarioInfo(1L, "Admin", "admin@canchas.com", Rol.ADMIN, true, null);
    }

    public static CanchaInfo cancha() {
        return new CanchaInfo(1L, "Sintética 1", 4.65, -74.05, false, EstadoCancha.ACTIVA);
    }

    public static CanchaInfo canchaTechada() {
        return new CanchaInfo(2L, "Coliseo", 4.65, -74.05, true, EstadoCancha.ACTIVA);
    }

    public static HorarioCancha horario(Long canchaId, LocalDate fecha) {
        return new HorarioCancha(canchaId, fecha, EstadoCancha.ACTIVA, h(6), h(22), List.of());
    }

    public static Reserva reserva(Long id, LocalDate fecha, int inicio, int fin, EstadoReserva estado) {
        return new Reserva(id, 1L, 2L, fecha, h(inicio), h(fin), 50_000L * (fin - inicio), estado, null, AHORA);
    }

    public static Reserva confirmada(Long id) {
        return reserva(id, FUTURO, 18, 20, EstadoReserva.CONFIRMADA);
    }

    public static void assertCodigo(CodigoError esperado, Executable accion) {
        ReservasException e = assertThrows(ReservasException.class, accion);
        assertEquals(esperado, e.getCodigo());
    }
}
