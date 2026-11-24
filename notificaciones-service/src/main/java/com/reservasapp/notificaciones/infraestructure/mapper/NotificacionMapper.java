package com.reservasapp.notificaciones.infraestructure.mapper;

import com.reservasapp.notificaciones.domain.model.ConteoEnvios;
import com.reservasapp.notificaciones.domain.model.EstadisticasNotificaciones;
import com.reservasapp.notificaciones.domain.model.Notificacion;
import com.reservasapp.notificaciones.domain.model.SolicitudNotificacion;
import com.reservasapp.notificaciones.domain.model.TipoNotificacion;
import com.reservasapp.notificaciones.infraestructure.driver_adapters.jpa_repository.NotificacionEntity;
import com.reservasapp.notificaciones.infraestructure.entry_points.dto.ConteoEnviosDTO;
import com.reservasapp.notificaciones.infraestructure.entry_points.dto.EstadisticasDTO;
import com.reservasapp.notificaciones.infraestructure.entry_points.dto.NotificacionDTO;
import com.reservasapp.notificaciones.infraestructure.entry_points.dto.SolicitudNotificacionRequest;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class NotificacionMapper {

    // ---- dominio <-> entidad JPA ----

    public NotificacionEntity toEntity(Notificacion n) {
        return NotificacionEntity.builder()
                .id(n.getId())
                .usuarioId(n.getUsuarioId())
                .canal(n.getCanal())
                .destinatario(n.getDestinatario())
                .tipo(n.getTipo())
                .mensaje(n.getMensaje())
                .estado(n.getEstado())
                .intentos(n.getIntentos())
                .fecha(n.getFecha())
                .build();
    }

    public Notificacion toDomain(NotificacionEntity e) {
        return Notificacion.builder()
                .id(e.getId())
                .usuarioId(e.getUsuarioId())
                .canal(e.getCanal())
                .destinatario(e.getDestinatario())
                .tipo(e.getTipo())
                .mensaje(e.getMensaje())
                .estado(e.getEstado())
                .intentos(e.getIntentos())
                .fecha(e.getFecha())
                .build();
    }

    // ---- request/response HTTP ----

    public SolicitudNotificacion toSolicitud(SolicitudNotificacionRequest r) {
        return new SolicitudNotificacion(
                r.usuarioId(), r.nombreUsuario(), r.canal(), r.destinatario(), r.reservaId(),
                r.nombreCancha(), r.fecha(), r.horaInicio(), r.horaFin(), r.precioTotal(),
                r.motivo(), r.fechaAnterior(), r.horaInicioAnterior(), r.probabilidadLluvia());
    }

    public NotificacionDTO toDto(Notificacion n) {
        return new NotificacionDTO(n.getId(), n.getUsuarioId(), n.getCanal(), n.getDestinatario(),
                n.getTipo(), n.getMensaje(), n.getEstado(), n.getIntentos(), n.getFecha());
    }

    public EstadisticasDTO toDto(EstadisticasNotificaciones e) {
        Map<String, ConteoEnviosDTO> porTipo = new LinkedHashMap<>();
        for (Map.Entry<TipoNotificacion, ConteoEnvios> entrada : e.porTipo().entrySet()) {
            porTipo.put(entrada.getKey().name(),
                    new ConteoEnviosDTO(entrada.getValue().enviadas(), entrada.getValue().fallidas()));
        }
        return new EstadisticasDTO(e.total(), e.enviadas(), e.fallidas(), porTipo);
    }
}
