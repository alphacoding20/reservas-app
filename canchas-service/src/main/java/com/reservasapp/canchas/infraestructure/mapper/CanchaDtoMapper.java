package com.reservasapp.canchas.infraestructure.mapper;

import com.reservasapp.canchas.domain.model.Cancha;
import com.reservasapp.canchas.infraestructure.entry_points.dto.ActualizarCanchaRequest;
import com.reservasapp.canchas.infraestructure.entry_points.dto.CanchaDTO;
import com.reservasapp.canchas.infraestructure.entry_points.dto.CrearCanchaRequest;
import lombok.experimental.UtilityClass;

import java.time.LocalTime;

@UtilityClass
public class CanchaDtoMapper {

    public Cancha toCancha(CrearCanchaRequest request) {
        return Cancha.builder()
                .nombre(request.nombre())
                .tipoDeporte(request.tipoDeporte())
                .direccion(request.direccion())
                .latitud(request.latitud())
                .longitud(request.longitud())
                .precioPorHora(request.precioPorHora())
                .techada(request.techada())
                .horaApertura(LocalTime.parse(request.horaApertura()))
                .horaCierre(LocalTime.parse(request.horaCierre()))
                .build();
    }

    public Cancha toCancha(ActualizarCanchaRequest request) {
        return Cancha.builder()
                .nombre(request.nombre())
                .tipoDeporte(request.tipoDeporte())
                .direccion(request.direccion())
                .latitud(request.latitud())
                .longitud(request.longitud())
                .precioPorHora(request.precioPorHora())
                .techada(request.techada())
                .horaApertura(LocalTime.parse(request.horaApertura()))
                .horaCierre(LocalTime.parse(request.horaCierre()))
                .estado(request.estado())
                .build();
    }

    public CanchaDTO toDto(Cancha cancha) {
        return new CanchaDTO(
                cancha.getId(),
                cancha.getNombre(),
                cancha.getTipoDeporte(),
                cancha.getDireccion(),
                cancha.getLatitud(),
                cancha.getLongitud(),
                cancha.getPrecioPorHora(),
                cancha.isTechada(),
                Formatos.hora(cancha.getHoraApertura()),
                Formatos.hora(cancha.getHoraCierre()),
                cancha.getEstado());
    }
}
