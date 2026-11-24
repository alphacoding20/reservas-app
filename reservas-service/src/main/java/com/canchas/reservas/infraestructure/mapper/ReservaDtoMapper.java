package com.canchas.reservas.infraestructure.mapper;

import com.canchas.reservas.domain.model.Disponibilidad;
import com.canchas.reservas.domain.model.Estadisticas;
import com.canchas.reservas.domain.model.Pronostico;
import com.canchas.reservas.domain.model.Reserva;
import com.canchas.reservas.domain.model.ResumenAlertaLluvia;
import com.canchas.reservas.domain.model.ResumenRecordatorios;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.AlertaLluviaDTO;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.DisponibilidadDTO;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.EstadisticasDTO;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.FranjaDTO;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.OcupacionCanchaDTO;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.PronosticoDTO;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.RecordatoriosDTO;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.ReservaDTO;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReservaDtoMapper {

    public ReservaDTO toDto(Reserva r) {
        return new ReservaDTO(r.id(), r.canchaId(), r.usuarioId(), r.fecha(), r.horaInicio(), r.horaFin(),
                r.precioTotal(), r.estado(), r.motivoCancelacion(), r.fechaCreacion());
    }

    public List<ReservaDTO> toDtos(List<Reserva> reservas) {
        return reservas.stream().map(this::toDto).toList();
    }

    public DisponibilidadDTO toDto(Disponibilidad d) {
        List<FranjaDTO> franjas = d.franjas().stream()
                .map(f -> new FranjaDTO(f.horaInicio(), f.horaFin(), f.disponible()))
                .toList();
        return new DisponibilidadDTO(d.canchaId(), d.fecha(), franjas);
    }

    public PronosticoDTO toDto(Pronostico p) {
        return new PronosticoDTO(p.reservaId(), p.aplica(), p.pronosticoDisponible(), p.probabilidadLluvia(),
                p.riesgoLluvia(), p.umbral());
    }

    public AlertaLluviaDTO toDto(ResumenAlertaLluvia r) {
        return new AlertaLluviaDTO(r.fecha(), r.reservasRevisadas(), r.alertasEnviadas(), r.sinPronostico());
    }

    public RecordatoriosDTO toDto(ResumenRecordatorios r) {
        return new RecordatoriosDTO(r.fecha(), r.recordatoriosEnviados());
    }

    public EstadisticasDTO toDto(Estadisticas e) {
        Map<String, Long> porEstado = new LinkedHashMap<>();
        e.porEstado().forEach((estado, total) -> porEstado.put(estado.name(), total));
        List<OcupacionCanchaDTO> ocupacion = e.ocupacionPorCancha().stream()
                .map(o -> new OcupacionCanchaDTO(o.canchaId(), o.horasReservadas()))
                .toList();
        return new EstadisticasDTO(e.desde(), e.hasta(), e.totalReservas(), porEstado, ocupacion);
    }
}
