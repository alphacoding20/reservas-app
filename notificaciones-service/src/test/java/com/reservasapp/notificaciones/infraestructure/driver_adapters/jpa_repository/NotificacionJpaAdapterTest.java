package com.reservasapp.notificaciones.infraestructure.driver_adapters.jpa_repository;

import com.reservasapp.notificaciones.TestData;
import com.reservasapp.notificaciones.domain.model.ConteoNotificacion;
import com.reservasapp.notificaciones.domain.model.EstadoNotificacion;
import com.reservasapp.notificaciones.domain.model.Notificacion;
import com.reservasapp.notificaciones.domain.model.TipoNotificacion;
import com.reservasapp.notificaciones.infraestructure.mapper.NotificacionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** El adaptador solo traduce entre dominio y Spring Data; el repositorio se simula (sin base de datos). */
@ExtendWith(MockitoExtension.class)
class NotificacionJpaAdapterTest {

    @Mock
    private SpringDataNotificacionRepository repository;

    private final NotificacionMapper mapper = new NotificacionMapper();
    private NotificacionJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new NotificacionJpaAdapter(repository, mapper);
    }

    @Test
    void guardarPersisteLaEntidadYDevuelveElDominioConIdAsignado() {
        NotificacionEntity guardada = mapper.toEntity(TestData.notificacion(null, EstadoNotificacion.ENVIADA));
        guardada.setId(10L);
        when(repository.save(any(NotificacionEntity.class))).thenReturn(guardada);

        Notificacion resultado = adapter.guardar(TestData.notificacion(null, EstadoNotificacion.ENVIADA));

        assertThat(resultado.getId()).isEqualTo(10L);
        assertThat(resultado.getEstado()).isEqualTo(EstadoNotificacion.ENVIADA);
    }

    @Test
    void buscarPorIdMapeaLaEntidadOVacio() {
        when(repository.findById(1L)).thenReturn(Optional.of(mapper.toEntity(TestData.notificacion(1L,
                EstadoNotificacion.ENVIADA))));
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThat(adapter.buscarPorId(1L)).get().extracting(Notificacion::getId).isEqualTo(1L);
        assertThat(adapter.buscarPorId(2L)).isEmpty();
    }

    @Test
    void buscarPorUsuarioConservaElOrdenDelRepositorio() {
        when(repository.findByUsuarioIdOrderByFechaDescIdDesc(3L)).thenReturn(List.of(
                mapper.toEntity(TestData.notificacion(2L, EstadoNotificacion.ENVIADA)),
                mapper.toEntity(TestData.notificacion(1L, EstadoNotificacion.FALLIDA))));

        assertThat(adapter.buscarPorUsuario(3L)).extracting(Notificacion::getId).containsExactly(2L, 1L);
    }

    @Test
    void existeYEliminarDelegan() {
        when(repository.existsById(1L)).thenReturn(true);

        assertThat(adapter.existePorId(1L)).isTrue();
        adapter.eliminarPorId(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void contarPorTipoYEstadoConvierteLasProyecciones() {
        ConteoProjection fila = new ConteoProjection() {
            @Override
            public TipoNotificacion getTipo() {
                return TipoNotificacion.CANCELACION;
            }

            @Override
            public EstadoNotificacion getEstado() {
                return EstadoNotificacion.FALLIDA;
            }

            @Override
            public long getCantidad() {
                return 4;
            }
        };
        when(repository.contarPorTipoYEstado()).thenReturn(List.of(fila));

        assertThat(adapter.contarPorTipoYEstado())
                .containsExactly(new ConteoNotificacion(TipoNotificacion.CANCELACION, EstadoNotificacion.FALLIDA, 4));
    }
}
