package com.reservasapp.canchas.infraestructure.mapper;

import com.reservasapp.canchas.domain.model.Bloqueo;
import com.reservasapp.canchas.infraestructure.driver_adapters.jpa_repository.BloqueoData;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BloqueoMapper {

    public Bloqueo toDomain(BloqueoData data) {
        if (data == null) {
            return null;
        }
        return Bloqueo.builder()
                .id(data.getId())
                .canchaId(data.getCanchaId())
                .fechaInicio(data.getFechaInicio())
                .fechaFin(data.getFechaFin())
                .motivo(data.getMotivo())
                .build();
    }

    public BloqueoData toData(Bloqueo bloqueo) {
        if (bloqueo == null) {
            return null;
        }
        return BloqueoData.builder()
                .id(bloqueo.getId())
                .canchaId(bloqueo.getCanchaId())
                .fechaInicio(bloqueo.getFechaInicio())
                .fechaFin(bloqueo.getFechaFin())
                .motivo(bloqueo.getMotivo())
                .build();
    }
}
