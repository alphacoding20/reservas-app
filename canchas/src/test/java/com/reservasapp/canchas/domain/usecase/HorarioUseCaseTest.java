package com.reservasapp.canchas.domain.usecase;

import com.reservasapp.canchas.domain.exception.NegocioException;
import com.reservasapp.canchas.domain.model.Bloqueo;
import com.reservasapp.canchas.domain.model.Cancha;
import com.reservasapp.canchas.domain.model.EstadoCancha;
import com.reservasapp.canchas.domain.model.FranjaBloqueada;
import com.reservasapp.canchas.domain.model.HorarioDia;
import com.reservasapp.canchas.domain.model.TipoDeporte;
import com.reservasapp.canchas.domain.model.gateway.BloqueoGateway;
import com.reservasapp.canchas.domain.model.gateway.CanchaGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HorarioUseCaseTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 12, 8);
    private static final LocalDateTime INICIO_DIA = LocalDateTime.of(2026, 12, 8, 0, 0);
    private static final LocalDateTime FIN_DIA = LocalDateTime.of(2026, 12, 9, 0, 0);

    @Mock
    private CanchaGateway canchaGateway;
    @Mock
    private BloqueoGateway bloqueoGateway;
    @InjectMocks
    private HorarioUseCase useCase;

    private Cancha cancha(EstadoCancha estado) {
        return Cancha.builder().id(1L).nombre("Sintética 1").tipoDeporte(TipoDeporte.FUTBOL_5)
                .precioPorHora(50000L).horaApertura(LocalTime.of(6, 0)).horaCierre(LocalTime.of(22, 0))
                .estado(estado).build();
    }

    private Bloqueo bloqueo(LocalDateTime inicio, LocalDateTime fin, String motivo) {
        return Bloqueo.builder().id(1L).canchaId(1L).fechaInicio(inicio).fechaFin(fin).motivo(motivo).build();
    }

    private void stubCancha(EstadoCancha estado) {
        when(canchaGateway.buscarPorId(1L)).thenReturn(Optional.of(cancha(estado)));
    }

    @Test
    void horario_sinBloqueos_devuelveHorarioDeLaCancha() {
        stubCancha(EstadoCancha.ACTIVA);
        when(bloqueoGateway.listarSolapados(1L, INICIO_DIA, FIN_DIA)).thenReturn(List.of());

        HorarioDia horario = useCase.horario(1L, FECHA);

        assertEquals(1L, horario.canchaId());
        assertEquals(FECHA, horario.fecha());
        assertEquals(EstadoCancha.ACTIVA, horario.estado());
        assertEquals(LocalTime.of(6, 0), horario.horaApertura());
        assertEquals(LocalTime.of(22, 0), horario.horaCierre());
        assertTrue(horario.bloqueos().isEmpty());
    }

    @Test
    void horario_bloqueoDentroDelDia_seDevuelveComoHoras() {
        stubCancha(EstadoCancha.ACTIVA);
        when(bloqueoGateway.listarSolapados(1L, INICIO_DIA, FIN_DIA)).thenReturn(List.of(
                bloqueo(LocalDateTime.of(2026, 12, 8, 14, 0), LocalDateTime.of(2026, 12, 8, 16, 0), "Mantenimiento")));

        List<FranjaBloqueada> franjas = useCase.horario(1L, FECHA).bloqueos();

        assertEquals(List.of(new FranjaBloqueada(LocalTime.of(14, 0), LocalTime.of(16, 0), "Mantenimiento")),
                franjas);
    }

    @Test
    void horario_bloqueoQueEmpezoAntes_seRecortaAlInicioDelDia() {
        stubCancha(EstadoCancha.ACTIVA);
        when(bloqueoGateway.listarSolapados(1L, INICIO_DIA, FIN_DIA)).thenReturn(List.of(
                bloqueo(LocalDateTime.of(2026, 12, 7, 20, 0), LocalDateTime.of(2026, 12, 8, 9, 0), "Obra")));

        FranjaBloqueada franja = useCase.horario(1L, FECHA).bloqueos().get(0);

        assertEquals(LocalTime.MIN, franja.horaInicio());
        assertEquals(LocalTime.of(9, 0), franja.horaFin());
    }

    @Test
    void horario_bloqueoQueSigueDespues_seRecortaAFinDelDia() {
        stubCancha(EstadoCancha.ACTIVA);
        when(bloqueoGateway.listarSolapados(1L, INICIO_DIA, FIN_DIA)).thenReturn(List.of(
                bloqueo(LocalDateTime.of(2026, 12, 8, 18, 0), LocalDateTime.of(2026, 12, 10, 6, 0), "Obra")));

        FranjaBloqueada franja = useCase.horario(1L, FECHA).bloqueos().get(0);

        assertEquals(LocalTime.of(18, 0), franja.horaInicio());
        assertEquals(LocalTime.of(23, 59), franja.horaFin());
    }

    @Test
    void horario_bloqueoQueTerminaALaMedianocheSiguiente_terminaALas2359() {
        stubCancha(EstadoCancha.ACTIVA);
        when(bloqueoGateway.listarSolapados(1L, INICIO_DIA, FIN_DIA)).thenReturn(List.of(
                bloqueo(LocalDateTime.of(2026, 12, 8, 20, 0), FIN_DIA, "Evento")));

        assertEquals(LocalTime.of(23, 59), useCase.horario(1L, FECHA).bloqueos().get(0).horaFin());
    }

    @Test
    void horario_canchaEnMantenimiento_respondeConSuEstado() {
        stubCancha(EstadoCancha.MANTENIMIENTO);
        when(bloqueoGateway.listarSolapados(1L, INICIO_DIA, FIN_DIA)).thenReturn(List.of());

        assertEquals(EstadoCancha.MANTENIMIENTO, useCase.horario(1L, FECHA).estado());
    }

    @Test
    void horario_canchaNoExiste_lanza404() {
        when(canchaGateway.buscarPorId(1L)).thenReturn(Optional.empty());

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.horario(1L, FECHA));

        assertEquals("CANCHA_NO_ENCONTRADA", ex.getCodigo());
        verifyNoInteractions(bloqueoGateway);
    }
}
