package com.reservasapp.canchas.infraestructure.driver_adapters;

import com.reservasapp.canchas.domain.model.Bloqueo;
import com.reservasapp.canchas.domain.model.Cancha;
import com.reservasapp.canchas.domain.model.EstadoCancha;
import com.reservasapp.canchas.domain.model.TipoDeporte;
import com.reservasapp.canchas.infraestructure.driver_adapters.jpa_repository.BloqueoData;
import com.reservasapp.canchas.infraestructure.driver_adapters.jpa_repository.BloqueoGatewayImpl;
import com.reservasapp.canchas.infraestructure.driver_adapters.jpa_repository.BloqueoJpaRepository;
import com.reservasapp.canchas.infraestructure.driver_adapters.jpa_repository.CanchaData;
import com.reservasapp.canchas.infraestructure.driver_adapters.jpa_repository.CanchaGatewayImpl;
import com.reservasapp.canchas.infraestructure.driver_adapters.jpa_repository.CanchaJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaGatewaysTest {

    @Mock
    private CanchaJpaRepository canchaRepository;
    @Mock
    private BloqueoJpaRepository bloqueoRepository;

    private CanchaData canchaData(Long id) {
        return CanchaData.builder().id(id).nombre("Sintética 1").tipoDeporte(TipoDeporte.FUTBOL_5)
                .direccion("Calle 1").latitud(4.33).longitud(-74.36).precioPorHora(50000L).techada(false)
                .horaApertura(LocalTime.of(6, 0)).horaCierre(LocalTime.of(22, 0))
                .estado(EstadoCancha.ACTIVA).build();
    }

    private BloqueoData bloqueoData(Long id) {
        return BloqueoData.builder().id(id).canchaId(1L)
                .fechaInicio(LocalDateTime.of(2026, 12, 8, 14, 0))
                .fechaFin(LocalDateTime.of(2026, 12, 8, 16, 0)).motivo("Mantenimiento").build();
    }

    // ---------- CanchaGatewayImpl ----------

    @Test
    void cancha_guardar_persisteYDevuelveElDominio() {
        CanchaGatewayImpl gateway = new CanchaGatewayImpl(canchaRepository);
        when(canchaRepository.save(any(CanchaData.class))).thenReturn(canchaData(1L));

        Cancha resultado = gateway.guardar(Cancha.builder().nombre("Sintética 1").build());

        assertEquals(1L, resultado.getId());
        assertEquals(TipoDeporte.FUTBOL_5, resultado.getTipoDeporte());
    }

    @Test
    void cancha_buscarPorId_existenteEInexistente() {
        CanchaGatewayImpl gateway = new CanchaGatewayImpl(canchaRepository);
        when(canchaRepository.findById(1L)).thenReturn(Optional.of(canchaData(1L)));
        when(canchaRepository.findById(2L)).thenReturn(Optional.empty());

        assertEquals("Sintética 1", gateway.buscarPorId(1L).orElseThrow().getNombre());
        assertTrue(gateway.buscarPorId(2L).isEmpty());
    }

    @Test
    void cancha_listar_yListarPorEstado() {
        CanchaGatewayImpl gateway = new CanchaGatewayImpl(canchaRepository);
        when(canchaRepository.findAllByOrderByIdAsc()).thenReturn(List.of(canchaData(1L), canchaData(2L)));
        when(canchaRepository.findByEstadoOrderByIdAsc(EstadoCancha.ACTIVA)).thenReturn(List.of(canchaData(1L)));

        assertEquals(2, gateway.listar().size());
        assertEquals(1, gateway.listarPorEstado(EstadoCancha.ACTIVA).size());
    }

    @Test
    void cancha_existePorId_yEliminar() {
        CanchaGatewayImpl gateway = new CanchaGatewayImpl(canchaRepository);
        when(canchaRepository.existsById(1L)).thenReturn(true);
        when(canchaRepository.existsById(2L)).thenReturn(false);

        assertTrue(gateway.existePorId(1L));
        assertFalse(gateway.existePorId(2L));
        gateway.eliminar(1L);
        verify(canchaRepository).deleteById(1L);
    }

    // ---------- BloqueoGatewayImpl ----------

    @Test
    void bloqueo_guardar_persisteYDevuelveElDominio() {
        BloqueoGatewayImpl gateway = new BloqueoGatewayImpl(bloqueoRepository);
        when(bloqueoRepository.save(any(BloqueoData.class))).thenReturn(bloqueoData(7L));

        Bloqueo resultado = gateway.guardar(Bloqueo.builder().canchaId(1L).motivo("Mantenimiento").build());

        assertEquals(7L, resultado.getId());
        assertEquals(1L, resultado.getCanchaId());
    }

    @Test
    void bloqueo_buscarPorId_existenteEInexistente() {
        BloqueoGatewayImpl gateway = new BloqueoGatewayImpl(bloqueoRepository);
        when(bloqueoRepository.findById(7L)).thenReturn(Optional.of(bloqueoData(7L)));
        when(bloqueoRepository.findById(8L)).thenReturn(Optional.empty());

        assertEquals(7L, gateway.buscarPorId(7L).orElseThrow().getId());
        assertTrue(gateway.buscarPorId(8L).isEmpty());
    }

    @Test
    void bloqueo_listarSolapados_pasaElRangoAlRepositorioEnElOrdenCorrecto() {
        BloqueoGatewayImpl gateway = new BloqueoGatewayImpl(bloqueoRepository);
        LocalDateTime desde = LocalDateTime.of(2026, 12, 8, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 12, 9, 0, 0);
        when(bloqueoRepository
                .findByCanchaIdAndFechaInicioLessThanAndFechaFinGreaterThanOrderByFechaInicioAsc(1L, hasta, desde))
                .thenReturn(List.of(bloqueoData(7L)));

        List<Bloqueo> resultado = gateway.listarSolapados(1L, desde, hasta);

        assertEquals(1, resultado.size());
        assertEquals(7L, resultado.get(0).getId());
    }

    @Test
    void bloqueo_eliminar_yEliminarPorCancha() {
        BloqueoGatewayImpl gateway = new BloqueoGatewayImpl(bloqueoRepository);

        gateway.eliminar(7L);
        gateway.eliminarPorCancha(1L);

        verify(bloqueoRepository).deleteById(7L);
        verify(bloqueoRepository).deleteByCanchaId(1L);
    }
}
