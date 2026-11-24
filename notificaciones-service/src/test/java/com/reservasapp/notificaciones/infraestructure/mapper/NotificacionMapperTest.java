package com.reservasapp.notificaciones.infraestructure.mapper;

import com.reservasapp.notificaciones.TestData;
import com.reservasapp.notificaciones.domain.model.ConteoEnvios;
import com.reservasapp.notificaciones.domain.model.EstadisticasNotificaciones;
import com.reservasapp.notificaciones.domain.model.EstadoNotificacion;
import com.reservasapp.notificaciones.domain.model.MotivoCancelacion;
import com.reservasapp.notificaciones.domain.model.Notificacion;
import com.reservasapp.notificaciones.domain.model.SolicitudNotificacion;
import com.reservasapp.notificaciones.domain.model.TipoNotificacion;
import com.reservasapp.notificaciones.infraestructure.driver_adapters.jpa_repository.NotificacionEntity;
import com.reservasapp.notificaciones.infraestructure.entry_points.dto.ConteoEnviosDTO;
import com.reservasapp.notificaciones.infraestructure.entry_points.dto.EstadisticasDTO;
import com.reservasapp.notificaciones.infraestructure.entry_points.dto.NotificacionDTO;
import com.reservasapp.notificaciones.infraestructure.entry_points.dto.SolicitudNotificacionRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NotificacionMapperTest {

    private final NotificacionMapper mapper = new NotificacionMapper();

    @Test
    void dominioAEntidadYDeVueltaConservaTodosLosCampos() {
        Notificacion original = TestData.notificacion(7L, EstadoNotificacion.FALLIDA);

        NotificacionEntity entidad = mapper.toEntity(original);
        Notificacion devuelta = mapper.toDomain(entidad);

        assertThat(entidad.getId()).isEqualTo(7L);
        assertThat(entidad.getEstado()).isEqualTo(EstadoNotificacion.FALLIDA);
        assertThat(devuelta).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    void dominioADtoExponeLosCamposDelContrato() {
        NotificacionDTO dto = mapper.toDto(TestData.notificacion(7L, EstadoNotificacion.ENVIADA));

        assertThat(dto.id()).isEqualTo(7L);
        assertThat(dto.usuarioId()).isEqualTo(3L);
        assertThat(dto.destinatario()).isEqualTo("123456789");
        assertThat(dto.tipo()).isEqualTo(TipoNotificacion.CONFIRMACION);
        assertThat(dto.mensaje()).isEqualTo("mensaje de prueba");
        assertThat(dto.estado()).isEqualTo(EstadoNotificacion.ENVIADA);
        assertThat(dto.intentos()).isEqualTo(1);
        assertThat(dto.fecha()).isEqualTo(TestData.AHORA);
    }

    @Test
    void requestASolicitudCopiaTodosLosCampos() {
        SolicitudNotificacionRequest request = new SolicitudNotificacionRequest(
                3L, "Ana Pérez", com.reservasapp.notificaciones.domain.model.Canal.TELEGRAM, "123456789", 55L,
                "Sintética 1", LocalDate.of(2026, 12, 8), LocalTime.of(18, 0), LocalTime.of(19, 0), 60000L,
                MotivoCancelacion.LLUVIA, LocalDate.of(2026, 12, 7), LocalTime.of(17, 0), 80);

        SolicitudNotificacion solicitud = mapper.toSolicitud(request);

        assertThat(solicitud).usingRecursiveComparison().isEqualTo(request);
    }

    @Test
    void estadisticasADtoUsaElNombreDelTipoComoLlave() {
        Map<TipoNotificacion, ConteoEnvios> porTipo = new EnumMap<>(TipoNotificacion.class);
        porTipo.put(TipoNotificacion.CONFIRMACION, new ConteoEnvios(4, 1));
        porTipo.put(TipoNotificacion.ALERTA_LLUVIA, new ConteoEnvios(0, 2));

        EstadisticasDTO dto = mapper.toDto(new EstadisticasNotificaciones(7, 4, 3, porTipo));

        assertThat(dto.total()).isEqualTo(7);
        assertThat(dto.enviadas()).isEqualTo(4);
        assertThat(dto.fallidas()).isEqualTo(3);
        assertThat(dto.porTipo()).containsOnlyKeys("CONFIRMACION", "ALERTA_LLUVIA");
        assertThat(dto.porTipo().get("CONFIRMACION")).isEqualTo(new ConteoEnviosDTO(4, 1));
    }
}
