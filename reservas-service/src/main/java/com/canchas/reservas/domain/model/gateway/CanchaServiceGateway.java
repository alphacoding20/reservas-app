package com.canchas.reservas.domain.model.gateway;

import com.canchas.reservas.domain.model.CanchaInfo;
import com.canchas.reservas.domain.model.HorarioCancha;

import java.time.LocalDate;

public interface CanchaServiceGateway {

    CanchaInfo obtenerCancha(Long canchaId);

    HorarioCancha obtenerHorario(Long canchaId, LocalDate fecha);

    long obtenerPrecioPorHora(Long canchaId, LocalDate fecha);
}
