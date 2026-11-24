package com.reservasapp.canchas.domain.usecase;

import com.reservasapp.canchas.domain.exception.NegocioException;
import com.reservasapp.canchas.domain.model.Cancha;
import com.reservasapp.canchas.domain.model.EstadoCancha;
import com.reservasapp.canchas.domain.model.TipoDeporte;
import com.reservasapp.canchas.domain.model.gateway.BloqueoGateway;
import com.reservasapp.canchas.domain.model.gateway.CanchaGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CanchaUseCaseTest {

    @Mock
    private CanchaGateway canchaGateway;
    @Mock
    private BloqueoGateway bloqueoGateway;
    @InjectMocks
    private CanchaUseCase useCase;

    private Cancha cancha(Long id, TipoDeporte deporte, boolean techada, long precio, int apertura, int cierre) {
        return Cancha.builder().id(id).nombre("Cancha " + id).tipoDeporte(deporte).direccion("Calle 1")
                .latitud(4.33).longitud(-74.36).precioPorHora(precio).techada(techada)
                .horaApertura(LocalTime.of(apertura, 0)).horaCierre(LocalTime.of(cierre, 0))
                .estado(EstadoCancha.ACTIVA).build();
    }

    private void verificarHorarioInvalido(Cancha cancha) {
        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.crear(cancha));
        assertEquals(400, ex.getStatus());
        assertEquals("HORARIO_INVALIDO", ex.getCodigo());
    }

    // ---------- crear ----------

    @Test
    void crear_ok_naceActivaYSinId() {
        Cancha entrada = cancha(99L, TipoDeporte.TENIS, false, 30000, 6, 22);
        entrada.setEstado(EstadoCancha.INACTIVA);
        when(canchaGateway.guardar(any(Cancha.class))).thenAnswer(inv -> inv.getArgument(0));

        Cancha resultado = useCase.crear(entrada);

        assertNull(resultado.getId());
        assertEquals(EstadoCancha.ACTIVA, resultado.getEstado());
    }

    @Test
    void crear_cierreHasta23_esValido() {
        when(canchaGateway.guardar(any(Cancha.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(LocalTime.of(23, 0),
                useCase.crear(cancha(null, TipoDeporte.TENIS, false, 30000, 6, 23)).getHoraCierre());
    }

    @Test
    void crear_horariosInvalidos_lanzan400YNoGuardan() {
        Cancha aperturaConMinutos = cancha(null, TipoDeporte.TENIS, false, 1000, 6, 22);
        aperturaConMinutos.setHoraApertura(LocalTime.of(6, 30));
        Cancha cierreConMinutos = cancha(null, TipoDeporte.TENIS, false, 1000, 6, 22);
        cierreConMinutos.setHoraCierre(LocalTime.of(22, 15));
        Cancha cierreIgualApertura = cancha(null, TipoDeporte.TENIS, false, 1000, 8, 8);
        Cancha cierreAntes = cancha(null, TipoDeporte.TENIS, false, 1000, 10, 8);
        Cancha cierreTarde = cancha(null, TipoDeporte.TENIS, false, 1000, 8, 23);
        cierreTarde.setHoraCierre(LocalTime.of(23, 30));
        Cancha sinApertura = cancha(null, TipoDeporte.TENIS, false, 1000, 6, 22);
        sinApertura.setHoraApertura(null);
        Cancha sinCierre = cancha(null, TipoDeporte.TENIS, false, 1000, 6, 22);
        sinCierre.setHoraCierre(null);

        verificarHorarioInvalido(aperturaConMinutos);
        verificarHorarioInvalido(cierreConMinutos);
        verificarHorarioInvalido(cierreIgualApertura);
        verificarHorarioInvalido(cierreAntes);
        verificarHorarioInvalido(cierreTarde);
        verificarHorarioInvalido(sinApertura);
        verificarHorarioInvalido(sinCierre);
        verifyNoInteractions(canchaGateway);
    }

    // ---------- obtener / listar ----------

    @Test
    void obtener_ok() {
        Cancha existente = cancha(1L, TipoDeporte.PADEL, false, 60000, 7, 22);
        when(canchaGateway.buscarPorId(1L)).thenReturn(Optional.of(existente));

        assertSame(existente, useCase.obtener(1L));
    }

    @Test
    void obtener_noExiste_lanza404() {
        when(canchaGateway.buscarPorId(1L)).thenReturn(Optional.empty());

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.obtener(1L));

        assertEquals(404, ex.getStatus());
        assertEquals("CANCHA_NO_ENCONTRADA", ex.getCodigo());
    }

    @Test
    void listar_devuelveTodas() {
        List<Cancha> todas = List.of(cancha(1L, TipoDeporte.PADEL, false, 60000, 7, 22));
        when(canchaGateway.listar()).thenReturn(todas);

        assertSame(todas, useCase.listar());
    }

    // ---------- actualizar ----------

    @Test
    void actualizar_ok_usaElIdDeLaRutaYGuardaElEstado() {
        when(canchaGateway.buscarPorId(1L))
                .thenReturn(Optional.of(cancha(1L, TipoDeporte.PADEL, false, 60000, 7, 22)));
        when(canchaGateway.guardar(any(Cancha.class))).thenAnswer(inv -> inv.getArgument(0));
        Cancha datos = cancha(77L, TipoDeporte.PADEL, false, 65000, 7, 22);
        datos.setEstado(EstadoCancha.MANTENIMIENTO);

        Cancha resultado = useCase.actualizar(1L, datos);

        assertEquals(1L, resultado.getId());
        assertEquals(EstadoCancha.MANTENIMIENTO, resultado.getEstado());
        assertEquals(65000L, resultado.getPrecioPorHora());
    }

    @Test
    void actualizar_noExiste_lanza404() {
        when(canchaGateway.buscarPorId(1L)).thenReturn(Optional.empty());

        NegocioException ex = assertThrows(NegocioException.class,
                () -> useCase.actualizar(1L, cancha(1L, TipoDeporte.PADEL, false, 60000, 7, 22)));

        assertEquals("CANCHA_NO_ENCONTRADA", ex.getCodigo());
        verify(canchaGateway, never()).guardar(any());
    }

    @Test
    void actualizar_horarioInvalido_lanza400() {
        when(canchaGateway.buscarPorId(1L))
                .thenReturn(Optional.of(cancha(1L, TipoDeporte.PADEL, false, 60000, 7, 22)));

        NegocioException ex = assertThrows(NegocioException.class,
                () -> useCase.actualizar(1L, cancha(1L, TipoDeporte.PADEL, false, 60000, 22, 7)));

        assertEquals("HORARIO_INVALIDO", ex.getCodigo());
        verify(canchaGateway, never()).guardar(any());
    }

    // ---------- eliminar ----------

    @Test
    void eliminar_ok_borraPrimeroLosBloqueosYLuegoLaCancha() {
        when(canchaGateway.existePorId(1L)).thenReturn(true);

        useCase.eliminar(1L);

        InOrder orden = inOrder(bloqueoGateway, canchaGateway);
        orden.verify(bloqueoGateway).eliminarPorCancha(1L);
        orden.verify(canchaGateway).eliminar(1L);
    }

    @Test
    void eliminar_noExiste_lanza404() {
        when(canchaGateway.existePorId(1L)).thenReturn(false);

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.eliminar(1L));

        assertEquals("CANCHA_NO_ENCONTRADA", ex.getCodigo());
        verifyNoInteractions(bloqueoGateway);
        verify(canchaGateway, never()).eliminar(any());
    }

    // ---------- buscar ----------

    private void stubActivas() {
        when(canchaGateway.listarPorEstado(EstadoCancha.ACTIVA)).thenReturn(List.of(
                cancha(1L, TipoDeporte.FUTBOL_5, false, 50000, 6, 22),
                cancha(2L, TipoDeporte.BALONCESTO, true, 40000, 8, 21),
                cancha(3L, TipoDeporte.PADEL, false, 60000, 7, 22)));
    }

    @Test
    void buscar_sinFiltros_devuelveTodasLasActivas() {
        stubActivas();

        assertEquals(3, useCase.buscar(null, null, null).size());
    }

    @Test
    void buscar_porDeporte() {
        stubActivas();

        List<Cancha> resultado = useCase.buscar(TipoDeporte.PADEL, null, null);

        assertEquals(1, resultado.size());
        assertEquals(3L, resultado.get(0).getId());
    }

    @Test
    void buscar_porTechada() {
        stubActivas();

        List<Cancha> resultado = useCase.buscar(null, true, null);

        assertEquals(1, resultado.size());
        assertEquals(2L, resultado.get(0).getId());
    }

    @Test
    void buscar_porPrecioMaximoInclusivo() {
        stubActivas();

        List<Cancha> resultado = useCase.buscar(null, null, 50000L);

        assertEquals(List.of(1L, 2L), resultado.stream().map(Cancha::getId).toList());
    }

    @Test
    void buscar_conVariosFiltros_losCombina() {
        stubActivas();

        assertEquals(0, useCase.buscar(TipoDeporte.PADEL, false, 50000L).size());
    }
}
