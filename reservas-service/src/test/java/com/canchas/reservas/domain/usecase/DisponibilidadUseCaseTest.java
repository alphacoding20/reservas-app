package com.canchas.reservas.domain.usecase;

import com.canchas.reservas.domain.exception.CodigoError;
import com.canchas.reservas.domain.exception.ReservasException;
import com.canchas.reservas.domain.model.Bloqueo;
import com.canchas.reservas.domain.model.Disponibilidad;
import com.canchas.reservas.domain.model.Disponibilidad.FranjaDisponible;
import com.canchas.reservas.domain.model.EstadoCancha;
import com.canchas.reservas.domain.model.EstadoReserva;
import com.canchas.reservas.domain.model.HorarioCancha;
import com.canchas.reservas.domain.model.gateway.CanchaServiceGateway;
import com.canchas.reservas.domain.model.gateway.ReservaGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.canchas.reservas.Fixtures.CLOCK;
import static com.canchas.reservas.Fixtures.FUTURO;
import static com.canchas.reservas.Fixtures.HOY;
import static com.canchas.reservas.Fixtures.assertCodigo;
import static com.canchas.reservas.Fixtures.h;
import static com.canchas.reservas.Fixtures.horario;
import static com.canchas.reservas.Fixtures.reserva;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DisponibilidadUseCaseTest {

    @Mock
    ReservaGateway reservas;
    @Mock
    CanchaServiceGateway canchas;

    DisponibilidadUseCase useCase;

    @BeforeEach
    void preparar() {
        useCase = new DisponibilidadUseCase(reservas, canchas, CLOCK);
        when(canchas.obtenerHorario(1L, FUTURO)).thenReturn(horario(1L, FUTURO));
        when(canchas.obtenerHorario(1L, HOY)).thenReturn(horario(1L, HOY));
        when(reservas.buscarConfirmadasPorCanchaYFecha(1L, FUTURO)).thenReturn(List.of());
        when(reservas.buscarConfirmadasPorCanchaYFecha(1L, HOY)).thenReturn(List.of());
    }

    private static boolean disponible(Disponibilidad d, int hora) {
        return d.franjas().stream().filter(f -> f.horaInicio().equals(h(hora))).findFirst().orElseThrow().disponible();
    }

    @Test
    void generaUnaFranjaPorHoraEntreAperturaYCierre() {
        Disponibilidad d = useCase.consultar(1L, FUTURO);

        assertEquals(16, d.franjas().size()); // 06:00 a 22:00
        assertEquals(new FranjaDisponible(h(6), h(7), true), d.franjas().get(0));
        assertEquals(new FranjaDisponible(h(21), h(22), true), d.franjas().get(15));
        assertTrue(d.franjas().stream().allMatch(FranjaDisponible::disponible));
    }

    @Test
    void marcaOcupadasLasFranjasQueChocanConReservasConfirmadas() {
        when(reservas.buscarConfirmadasPorCanchaYFecha(1L, FUTURO))
                .thenReturn(List.of(reserva(1L, FUTURO, 18, 20, EstadoReserva.CONFIRMADA)));

        Disponibilidad d = useCase.consultar(1L, FUTURO);

        assertFalse(disponible(d, 18));
        assertFalse(disponible(d, 19));
        assertTrue(disponible(d, 17));
        assertTrue(disponible(d, 20));
    }

    @Test
    void marcaNoDisponiblesLasFranjasConBloqueo() {
        when(canchas.obtenerHorario(1L, FUTURO)).thenReturn(new HorarioCancha(1L, FUTURO, EstadoCancha.ACTIVA, h(6),
                h(22), List.of(new Bloqueo(h(14), h(16), "Mantenimiento"))));

        Disponibilidad d = useCase.consultar(1L, FUTURO);

        assertFalse(disponible(d, 14));
        assertFalse(disponible(d, 15));
        assertTrue(disponible(d, 16));
    }

    @Test
    void lasFranjasQueYaEmpezaronHoyNoEstanDisponibles() {
        Disponibilidad d = useCase.consultar(1L, HOY); // ahora son las 10:00

        assertFalse(disponible(d, 6));
        assertFalse(disponible(d, 9));
        assertTrue(disponible(d, 10));
        assertTrue(disponible(d, 15));
    }

    @Test
    void canchaQueNoEstaActivaDevuelveFranjasVacias() {
        when(canchas.obtenerHorario(1L, FUTURO)).thenReturn(new HorarioCancha(1L, FUTURO,
                EstadoCancha.MANTENIMIENTO, h(6), h(22), null));

        assertTrue(useCase.consultar(1L, FUTURO).franjas().isEmpty());
    }

    @Test
    void ignoraFranjasIncompletasAlFinalDelHorario() {
        when(canchas.obtenerHorario(1L, FUTURO)).thenReturn(new HorarioCancha(1L, FUTURO, EstadoCancha.ACTIVA, h(6),
                LocalTime.of(7, 30), List.of()));
        assertEquals(1, useCase.consultar(1L, FUTURO).franjas().size());

        when(canchas.obtenerHorario(1L, FUTURO)).thenReturn(new HorarioCancha(1L, FUTURO, EstadoCancha.ACTIVA, h(23),
                LocalTime.of(23, 59), List.of()));
        assertTrue(useCase.consultar(1L, FUTURO).franjas().isEmpty());
    }

    @Test
    void propagaCanchaInexistenteYValidaParametros() {
        LocalDate otra = FUTURO.plusDays(1);
        when(canchas.obtenerHorario(99L, otra))
                .thenThrow(new ReservasException(CodigoError.CANCHA_NO_ENCONTRADA, "no existe"));

        assertCodigo(CodigoError.CANCHA_NO_ENCONTRADA, () -> useCase.consultar(99L, otra));
        assertCodigo(CodigoError.VALIDACION, () -> useCase.consultar(null, FUTURO));
        assertCodigo(CodigoError.VALIDACION, () -> useCase.consultar(1L, null));
    }
}
