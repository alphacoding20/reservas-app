package com.reservasapp.canchas.domain.model.gateway;

import com.reservasapp.canchas.domain.model.Festivo;

import java.util.List;

public interface FestivoGateway {

    List<Festivo> festivosDelAnio(int anio);
}
