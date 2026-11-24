package com.canchas.reservas.domain.usecase;

import com.canchas.reservas.domain.exception.CodigoError;
import com.canchas.reservas.domain.model.Estadisticas;
import com.canchas.reservas.domain.model.EstadoReserva;
import com.canchas.reservas.domain.model.Reserva;
import com.canchas.reservas.domain.model.gateway.ReservaGateway;
import com.canchas.reservas.domain.model.gateway.UsuarioServiceGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static com.canchas.reservas.Fixtures.AHORA;
import static com.canchas.reservas.Fixtures.FUTURO;
import static com.canchas.reservas.Fixtures.HOY;
import static com.canchas.reservas.Fixtures.admin;
import static com.canchas.reservas.Fixtures.assertCodigo;
import static com.canchas.reservas.Fixtures.h;
import static com.canchas.reservas.Fixtures.reserva;
import static com.canchas.reservas.Fixtures.usuario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EstadisticasUseCaseTest {

    @Mock
    ReservaGateway reservas;
    @Mock
    UsuarioServiceGateway usuarios;

    EstadisticasUseCase useCase;

    @BeforeEach
    void preparar() {
        useCase = new EstadisticasUseCase(reservas, new ValidadorAdmin(usuarios));
        when(usuarios.validar(1L)).thenReturn(admin());
        when(usuarios.validar(2L)).thenReturn(usuario());
    }

    private static Reserva enCancha(Long canchaId, int inicio, int fin, EstadoReserva estado) {
        return new Reserva(null, canchaId, 2L, FUTURO, h(inicio), h(fin), 1L, estado, null, AHORA);
    }

    @Test
    void cuentaPorEstadoYSumaHorasOcupadasPorCanchaIgnorandoLasCanceladas() {
        when(reservas.buscarPorRangoFechas(HOY, FUTURO)).thenReturn(List.of(
                enCancha(2L, 10, 13, EstadoReserva.COMPLETADA),   // 3 h cancha 2
                enCancha(1L, 18, 20, EstadoReserva.CONFIRMADA),   // 2 h cancha 1
                enCancha(1L, 8, 9, EstadoReserva.CANCELADA),      // no cuenta horas
                enCancha(1L, 9, 10, EstadoReserva.NO_ASISTIO)));  // 1 h cancha 1

        Estadisticas e = useCase.consultar(1L, HOY, FUTURO);

        assertEquals(4, e.totalReservas());
        assertEquals(4, e.porEstado().size());
        assertEquals(1L, e.porEstado().get(EstadoReserva.CONFIRMADA));
        assertEquals(1L, e.porEstado().get(EstadoReserva.CANCELADA));
        assertEquals(1L, e.porEstado().get(EstadoReserva.COMPLETADA));
        assertEquals(1L, e.porEstado().get(EstadoReserva.NO_ASISTIO));
        assertEquals(2, e.ocupacionPorCancha().size());
        assertEquals(1L, e.ocupacionPorCancha().get(0).canchaId());
        assertEquals(3L, e.ocupacionPorCancha().get(0).horasReservadas());
        assertEquals(2L, e.ocupacionPorCancha().get(1).canchaId());
        assertEquals(3L, e.ocupacionPorCancha().get(1).horasReservadas());
    }

    @Test
    void sinReservasDevuelveTodosLosEstadosEnCero() {
        when(reservas.buscarPorRangoFechas(HOY, FUTURO)).thenReturn(List.of());

        Estadisticas e = useCase.consultar(1L, HOY, FUTURO);

        assertEquals(0, e.totalReservas());
        assertEquals(0L, e.porEstado().get(EstadoReserva.CONFIRMADA));
        assertEquals(0, e.ocupacionPorCancha().size());
    }

    @Test
    void validaAdminYRango() {
        assertCodigo(CodigoError.SIN_CREDENCIALES, () -> useCase.consultar(null, HOY, FUTURO));
        assertCodigo(CodigoError.PERMISO_DENEGADO, () -> useCase.consultar(2L, HOY, FUTURO));
        assertCodigo(CodigoError.VALIDACION, () -> useCase.consultar(1L, FUTURO, HOY));
        assertCodigo(CodigoError.VALIDACION, () -> useCase.consultar(1L, null, HOY));
        assertCodigo(CodigoError.VALIDACION, () -> useCase.consultar(1L, HOY, null));
    }

    @Test
    void elReservaDeUnSoloDiaEsUnRangoValido() {
        when(reservas.buscarPorRangoFechas(HOY, HOY)).thenReturn(List.of(reserva(1L, HOY, 12, 13, EstadoReserva.CONFIRMADA)));
        assertEquals(1, useCase.consultar(1L, HOY, HOY).totalReservas());
    }
}
