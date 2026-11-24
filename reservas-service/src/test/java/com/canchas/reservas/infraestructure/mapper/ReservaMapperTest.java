package com.canchas.reservas.infraestructure.mapper;

import com.canchas.reservas.domain.model.Reserva;
import com.canchas.reservas.domain.model.EstadoReserva;
import com.canchas.reservas.domain.model.MotivoCancelacion;
import com.canchas.reservas.infraestructure.driver_adapters.jpa_repository.ReservaData;
import org.junit.jupiter.api.Test;

import static com.canchas.reservas.Fixtures.AHORA;
import static com.canchas.reservas.Fixtures.FUTURO;
import static com.canchas.reservas.Fixtures.confirmada;
import static com.canchas.reservas.Fixtures.h;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ReservaMapperTest {

    private final ReservaMapper mapper = new ReservaMapper();

    @Test
    void convierteDominioAEntidad() {
        ReservaData data = mapper.toData(confirmada(5L).cancelada(MotivoCancelacion.ADMIN));

        assertEquals(5L, data.getId());
        assertEquals(1L, data.getCanchaId());
        assertEquals(2L, data.getUsuarioId());
        assertEquals(FUTURO, data.getFecha());
        assertEquals(h(18), data.getHoraInicio());
        assertEquals(h(20), data.getHoraFin());
        assertEquals(100_000L, data.getPrecioTotal());
        assertEquals(EstadoReserva.CANCELADA, data.getEstado());
        assertEquals(MotivoCancelacion.ADMIN, data.getMotivoCancelacion());
        assertEquals(AHORA, data.getFechaCreacion());
    }

    @Test
    void convierteEntidadADominioYElViajeDeIdaYVueltaConservaTodo() {
        Reserva original = confirmada(5L);
        Reserva vuelta = mapper.toReserva(mapper.toData(original));

        assertEquals(original, vuelta);
        assertNull(vuelta.motivoCancelacion());
    }

    @Test
    void unaReservaNuevaConservaElIdNulo() {
        Reserva nueva = new Reserva(null, 1L, 2L, FUTURO, h(8), h(9), 1L, EstadoReserva.CONFIRMADA, null, AHORA);
        assertNull(mapper.toData(nueva).getId());
    }
}
