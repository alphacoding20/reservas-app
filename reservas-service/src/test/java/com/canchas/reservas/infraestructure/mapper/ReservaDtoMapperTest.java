package com.canchas.reservas.infraestructure.mapper;

import com.canchas.reservas.domain.model.Disponibilidad;
import com.canchas.reservas.domain.model.Disponibilidad.FranjaDisponible;
import com.canchas.reservas.domain.model.Estadisticas;
import com.canchas.reservas.domain.model.Estadisticas.OcupacionCancha;
import com.canchas.reservas.domain.model.EstadoReserva;
import com.canchas.reservas.domain.model.Pronostico;
import com.canchas.reservas.domain.model.ResumenAlertaLluvia;
import com.canchas.reservas.domain.model.ResumenRecordatorios;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.DisponibilidadDTO;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.EstadisticasDTO;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.ReservaDTO;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.canchas.reservas.Fixtures.FUTURO;
import static com.canchas.reservas.Fixtures.confirmada;
import static com.canchas.reservas.Fixtures.h;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservaDtoMapperTest {

    private final ReservaDtoMapper mapper = new ReservaDtoMapper();

    @Test
    void reservaYListaDeReservas() {
        ReservaDTO dto = mapper.toDto(confirmada(3L));

        assertEquals(3L, dto.id());
        assertEquals(1L, dto.canchaId());
        assertEquals(2L, dto.usuarioId());
        assertEquals(FUTURO, dto.fecha());
        assertEquals(h(18), dto.horaInicio());
        assertEquals(100_000L, dto.precioTotal());
        assertEquals(EstadoReserva.CONFIRMADA, dto.estado());
        assertNull(dto.motivoCancelacion());
        assertEquals(2, mapper.toDtos(List.of(confirmada(1L), confirmada(2L))).size());
    }

    @Test
    void disponibilidad() {
        DisponibilidadDTO dto = mapper.toDto(new Disponibilidad(1L, FUTURO,
                List.of(new FranjaDisponible(h(6), h(7), true), new FranjaDisponible(h(7), h(8), false))));

        assertEquals(2, dto.franjas().size());
        assertTrue(dto.franjas().get(0).disponible());
        assertFalse(dto.franjas().get(1).disponible());
        assertEquals(h(7), dto.franjas().get(1).horaInicio());
    }

    @Test
    void pronosticoYResumenes() {
        var p = mapper.toDto(new Pronostico(1L, true, false, null, false, 60));
        assertTrue(p.aplica());
        assertFalse(p.pronosticoDisponible());
        assertNull(p.probabilidadLluvia());
        assertEquals(60, p.umbral());

        var a = mapper.toDto(new ResumenAlertaLluvia(FUTURO, 4, 2, 1));
        assertEquals(4, a.reservasRevisadas());
        assertEquals(2, a.alertasEnviadas());
        assertEquals(1, a.sinPronostico());

        assertEquals(7, mapper.toDto(new ResumenRecordatorios(FUTURO, 7)).recordatoriosEnviados());
    }

    @Test
    void estadisticasConservaElOrdenDeLosEstados() {
        Map<EstadoReserva, Long> porEstado = new EnumMap<>(EstadoReserva.class);
        for (EstadoReserva e : EstadoReserva.values()) {
            porEstado.put(e, 1L);
        }
        EstadisticasDTO dto = mapper.toDto(new Estadisticas(FUTURO, FUTURO.plusDays(1), 4, porEstado,
                List.of(new OcupacionCancha(1L, 5))));

        assertEquals(List.of("CONFIRMADA", "CANCELADA", "COMPLETADA", "NO_ASISTIO"),
                List.copyOf(dto.porEstado().keySet()));
        assertEquals(5L, dto.ocupacionPorCancha().get(0).horasReservadas());
        assertEquals(4L, dto.totalReservas());
    }
}
