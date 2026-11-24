package com.reservasapp.canchas.infraestructure.mapper;

import com.reservasapp.canchas.domain.model.Cancha;
import com.reservasapp.canchas.infraestructure.driver_adapters.jpa_repository.CanchaData;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CanchaMapper {

    public Cancha toDomain(CanchaData data) {
        if (data == null) {
            return null;
        }
        return Cancha.builder()
                .id(data.getId())
                .nombre(data.getNombre())
                .tipoDeporte(data.getTipoDeporte())
                .direccion(data.getDireccion())
                .latitud(data.getLatitud())
                .longitud(data.getLongitud())
                .precioPorHora(data.getPrecioPorHora())
                .techada(data.isTechada())
                .horaApertura(data.getHoraApertura())
                .horaCierre(data.getHoraCierre())
                .estado(data.getEstado())
                .build();
    }

    public CanchaData toData(Cancha cancha) {
        if (cancha == null) {
            return null;
        }
        return CanchaData.builder()
                .id(cancha.getId())
                .nombre(cancha.getNombre())
                .tipoDeporte(cancha.getTipoDeporte())
                .direccion(cancha.getDireccion())
                .latitud(cancha.getLatitud())
                .longitud(cancha.getLongitud())
                .precioPorHora(cancha.getPrecioPorHora())
                .techada(cancha.isTechada())
                .horaApertura(cancha.getHoraApertura())
                .horaCierre(cancha.getHoraCierre())
                .estado(cancha.getEstado())
                .build();
    }
}
