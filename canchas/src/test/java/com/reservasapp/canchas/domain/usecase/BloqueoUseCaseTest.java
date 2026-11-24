package com.reservasapp.canchas.domain.usecase;

import com.reservasapp.canchas.domain.exception.NegocioException;
import com.reservasapp.canchas.domain.model.Bloqueo;
import com.reservasapp.canchas.domain.model.gateway.BloqueoGateway;
import com.reservasapp.canchas.domain.model.gateway.CanchaGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BloqueoUseCaseTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 12, 8, 14, 0);
    private static final LocalDateTime FIN = LocalDateTime.of(2026, 12, 8, 16, 0);

    @Mock
    private CanchaGateway canchaGateway;
    @Mock
    private BloqueoGateway bloqueoGateway;
    @InjectMocks
    private BloqueoUseCase useCase;

    private Bloqueo bloqueo(Long id, Long canchaId) {
        return Bloqueo.builder().id(id).canchaId(canchaId).fechaInicio(INICIO).fechaFin(FIN)
                .motivo("Mantenimiento").build();
    }

    @Test
    void crear_ok_guardaElBloqueoDeLaCancha() {
        when(canchaGateway.existePorId(1L)).thenReturn(true);
        when(bloqueoGateway.guardar(any(Bloqueo.class))).thenAnswer(inv -> inv.getArgument(0));

        Bloqueo resultado = useCase.crear(1L, INICIO, FIN, "Mantenimiento");

        ArgumentCaptor<Bloqueo> captor = ArgumentCaptor.forClass(Bloqueo.class);
        verify(bloqueoGateway).guardar(captor.capture());
        assertEquals(1L, captor.getValue().getCanchaId());
        assertEquals(INICIO, resultado.getFechaInicio());
        assertEquals(FIN, resultado.getFechaFin());
        assertEquals("Mantenimiento", resultado.getMotivo());
    }

    @Test
    void crear_canchaNoExiste_lanza404() {
        when(canchaGateway.existePorId(1L)).thenReturn(false);

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.crear(1L, INICIO, FIN, "x"));

        assertEquals(404, ex.getStatus());
        assertEquals("CANCHA_NO_ENCONTRADA", ex.getCodigo());
        verify(bloqueoGateway, never()).guardar(any());
    }

    private void verificarInvalido(LocalDateTime inicio, LocalDateTime fin) {
        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.crear(1L, inicio, fin, "x"));
        assertEquals(400, ex.getStatus());
        assertEquals("HORARIO_INVALIDO", ex.getCodigo());
    }

    @Test
    void crear_fechasInvalidas_lanzan400YNoGuardan() {
        when(canchaGateway.existePorId(1L)).thenReturn(true);

        verificarInvalido(INICIO.withMinute(30), FIN);
        verificarInvalido(INICIO, FIN.withMinute(15));
        verificarInvalido(INICIO.withSecond(30), FIN);
        verificarInvalido(INICIO, FIN.withNano(1000));
        verificarInvalido(FIN, INICIO);
        verificarInvalido(INICIO, INICIO);
        verify(bloqueoGateway, never()).guardar(any());
    }

    @Test
    void crear_bloqueoQueCruzaDias_esValido() {
        when(canchaGateway.existePorId(1L)).thenReturn(true);
        when(bloqueoGateway.guardar(any(Bloqueo.class))).thenAnswer(inv -> inv.getArgument(0));

        Bloqueo resultado = useCase.crear(1L, LocalDateTime.of(2026, 12, 8, 22, 0),
                LocalDateTime.of(2026, 12, 9, 6, 0), "Mantenimiento mayor");

        assertEquals(LocalDateTime.of(2026, 12, 9, 6, 0), resultado.getFechaFin());
    }

    @Test
    void eliminar_ok() {
        when(bloqueoGateway.buscarPorId(5L)).thenReturn(Optional.of(bloqueo(5L, 1L)));

        useCase.eliminar(1L, 5L);

        verify(bloqueoGateway).eliminar(5L);
    }

    @Test
    void eliminar_bloqueoNoExiste_lanza404() {
        when(bloqueoGateway.buscarPorId(5L)).thenReturn(Optional.empty());

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.eliminar(1L, 5L));

        assertEquals(404, ex.getStatus());
        assertEquals("BLOQUEO_NO_ENCONTRADO", ex.getCodigo());
        verify(bloqueoGateway, never()).eliminar(any());
    }

    @Test
    void eliminar_bloqueoDeOtraCancha_lanza404() {
        when(bloqueoGateway.buscarPorId(5L)).thenReturn(Optional.of(bloqueo(5L, 2L)));

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.eliminar(1L, 5L));

        assertEquals("BLOQUEO_NO_ENCONTRADO", ex.getCodigo());
        verify(bloqueoGateway, never()).eliminar(any());
    }
}
