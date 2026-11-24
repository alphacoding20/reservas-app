package com.canchas.reservas.infraestructure.driver_adapters.jpa_repository;

import com.canchas.reservas.domain.model.EstadoReserva;
import com.canchas.reservas.domain.model.Reserva;
import com.canchas.reservas.infraestructure.mapper.ReservaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.canchas.reservas.Fixtures.AHORA;
import static com.canchas.reservas.Fixtures.FUTURO;
import static com.canchas.reservas.Fixtures.confirmada;
import static com.canchas.reservas.Fixtures.h;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReservaDataGatewayImplTest {

    @Mock
    ReservaJpaRepository repository;

    ReservaMapper mapper = new ReservaMapper();
    ReservaDataGatewayImpl gateway;

    @BeforeEach
    void preparar() {
        gateway = new ReservaDataGatewayImpl(repository, mapper);
    }

    private ReservaData data(Long id) {
        return mapper.toData(confirmada(id));
    }

    @Test
    void guardarConvierteYDevuelveLaEntidadGuardada() {
        when(repository.save(any(ReservaData.class))).thenReturn(data(9L));

        Reserva guardada = gateway.guardar(confirmada(null));

        assertEquals(9L, guardada.id());
    }

    @Test
    void buscarPorIdMapeaOVacio() {
        when(repository.findById(1L)).thenReturn(Optional.of(data(1L)));
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertEquals(1L, gateway.buscarPorId(1L).orElseThrow().id());
        assertTrue(gateway.buscarPorId(2L).isEmpty());
    }

    @Test
    void listarTodasPideOrdenPorId() {
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(data(1L), data(2L)));

        assertEquals(2, gateway.listarTodas().size());
        verify(repository).findAll(Sort.by("id"));
    }

    @Test
    void eliminarDelegaEnElRepositorio() {
        gateway.eliminar(4L);
        verify(repository).deleteById(4L);
    }

    @Test
    void solapamientoSoloConsideraConfirmadasYUsaCentinelaSinExclusion() {
        when(repository.contarSolapadas(1L, FUTURO, EstadoReserva.CONFIRMADA, h(18), h(19), -1L)).thenReturn(1L);
        when(repository.contarSolapadas(1L, FUTURO, EstadoReserva.CONFIRMADA, h(18), h(19), 5L)).thenReturn(0L);

        assertTrue(gateway.existeSolapamiento(1L, FUTURO, h(18), h(19), null));
        assertFalse(gateway.existeSolapamiento(1L, FUTURO, h(18), h(19), 5L));
    }

    @Test
    void contarFuturasSeparaFechaYHoraDeAhora() {
        when(repository.contarFuturas(2L, EstadoReserva.CONFIRMADA, AHORA.toLocalDate(), AHORA.toLocalTime()))
                .thenReturn(2L);

        assertEquals(2L, gateway.contarConfirmadasFuturas(2L, AHORA));
    }

    @Test
    void lasConsultasDeListaMapeanYUsanLosFiltrosCorrectos() {
        LocalDate hasta = FUTURO.plusDays(3);
        when(repository.findByUsuarioIdOrderByFechaDescHoraInicioDesc(2L)).thenReturn(List.of(data(1L)));
        when(repository.findByFechaAndEstadoOrderByHoraInicioAsc(FUTURO, EstadoReserva.CONFIRMADA))
                .thenReturn(List.of(data(1L), data(2L)));
        when(repository.findByCanchaIdAndFechaAndEstadoOrderByHoraInicioAsc(1L, FUTURO, EstadoReserva.CONFIRMADA))
                .thenReturn(List.of(data(3L)));
        when(repository.findByFechaBetween(FUTURO, hasta)).thenReturn(List.of(data(1L), data(2L), data(3L)));

        assertEquals(1, gateway.buscarPorUsuario(2L).size());
        assertEquals(2, gateway.buscarConfirmadasPorFecha(FUTURO).size());
        assertEquals(3L, gateway.buscarConfirmadasPorCanchaYFecha(1L, FUTURO).get(0).id());
        assertEquals(3, gateway.buscarPorRangoFechas(FUTURO, hasta).size());
    }
}
