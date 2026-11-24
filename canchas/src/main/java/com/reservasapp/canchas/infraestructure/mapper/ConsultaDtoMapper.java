package com.reservasapp.canchas.infraestructure.mapper;

import com.reservasapp.canchas.domain.model.Bloqueo;
import com.reservasapp.canchas.domain.model.Festivo;
import com.reservasapp.canchas.domain.model.HorarioDia;
import com.reservasapp.canchas.domain.model.Tarifa;
import com.reservasapp.canchas.infraestructure.entry_points.dto.BloqueoDTO;
import com.reservasapp.canchas.infraestructure.entry_points.dto.BloqueoHoraDTO;
import com.reservasapp.canchas.infraestructure.entry_points.dto.FestivoDTO;
import com.reservasapp.canchas.infraestructure.entry_points.dto.HorarioDTO;
import com.reservasapp.canchas.infraestructure.entry_points.dto.TarifaDTO;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConsultaDtoMapper {

    public HorarioDTO toDto(HorarioDia horario) {
        return new HorarioDTO(
                horario.canchaId(),
                Formatos.fecha(horario.fecha()),
                horario.estado(),
                Formatos.hora(horario.horaApertura()),
                Formatos.hora(horario.horaCierre()),
                horario.bloqueos().stream()
                        .map(franja -> new BloqueoHoraDTO(
                                Formatos.hora(franja.horaInicio()),
                                Formatos.hora(franja.horaFin()),
                                franja.motivo()))
                        .toList());
    }

    public TarifaDTO toDto(Tarifa tarifa) {
        return new TarifaDTO(
                tarifa.canchaId(),
                Formatos.fecha(tarifa.fecha()),
                tarifa.precioBase(),
                tarifa.factor(),
                tarifa.precioPorHora(),
                tarifa.esFestivo(),
                tarifa.esFinDeSemana(),
                tarifa.nombreFestivo());
    }

    public BloqueoDTO toDto(Bloqueo bloqueo) {
        return new BloqueoDTO(
                bloqueo.getId(),
                bloqueo.getCanchaId(),
                Formatos.fechaHora(bloqueo.getFechaInicio()),
                Formatos.fechaHora(bloqueo.getFechaFin()),
                bloqueo.getMotivo());
    }

    public FestivoDTO toDto(Festivo festivo) {
        return new FestivoDTO(Formatos.fecha(festivo.fecha()), festivo.nombre());
    }
}
