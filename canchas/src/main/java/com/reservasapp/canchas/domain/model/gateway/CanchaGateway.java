package com.reservasapp.canchas.domain.model.gateway;

import com.reservasapp.canchas.domain.model.Cancha;
import com.reservasapp.canchas.domain.model.EstadoCancha;

import java.util.List;
import java.util.Optional;

public interface CanchaGateway {
    Cancha guardar(Cancha cancha);

    Optional<Cancha> buscarPorId(Long id);

    List<Cancha> listar();

    List<Cancha> listarPorEstado(EstadoCancha estado);

    boolean existePorId(Long id);

    void eliminar(Long id);
}
