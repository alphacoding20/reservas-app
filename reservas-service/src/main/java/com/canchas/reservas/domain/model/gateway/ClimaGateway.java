package com.canchas.reservas.domain.model.gateway;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public interface ClimaGateway {

    Optional<Integer> consultarProbabilidadLluvia(Double latitud, Double longitud, LocalDate fecha,
                                                  LocalTime horaInicio, LocalTime horaFin);
}
