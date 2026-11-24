package com.reservasapp.notificaciones;

import com.reservasapp.notificaciones.domain.model.Canal;
import com.reservasapp.notificaciones.domain.model.EstadoNotificacion;
import com.reservasapp.notificaciones.domain.model.MotivoCancelacion;
import com.reservasapp.notificaciones.domain.model.Notificacion;
import com.reservasapp.notificaciones.domain.model.SolicitudNotificacion;
import com.reservasapp.notificaciones.domain.model.TipoNotificacion;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/** Datos de prueba compartidos. Reloj fijo: 2026-12-01 10:15:30 hora de Bogota. */
public final class TestData {

    public static final Clock CLOCK = Clock.fixed(Instant.parse("2026-12-01T15:15:30Z"), ZoneId.of("America/Bogota"));
    public static final LocalDateTime AHORA = LocalDateTime.of(2026, 12, 1, 10, 15, 30);

    private TestData() {
    }

    /** Solicitud con TODOS los campos; cada test usa solo los que le corresponden. */
    public static SolicitudNotificacion solicitudCompleta() {
        return new SolicitudNotificacion(3L, "Ana Pérez", Canal.TELEGRAM, "123456789", 55L, "Sintética 1",
                LocalDate.of(2026, 12, 8), LocalTime.of(18, 0), LocalTime.of(19, 0), 60000L,
                MotivoCancelacion.USUARIO, LocalDate.of(2026, 12, 7), LocalTime.of(17, 0), 70);
    }

    public static Notificacion notificacion(Long id, EstadoNotificacion estado) {
        return Notificacion.builder()
                .id(id)
                .usuarioId(3L)
                .canal(Canal.TELEGRAM)
                .destinatario("123456789")
                .tipo(TipoNotificacion.CONFIRMACION)
                .mensaje("mensaje de prueba")
                .estado(estado)
                .intentos(1)
                .fecha(AHORA)
                .build();
    }
}
