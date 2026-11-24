package com.reservasapp.canchas.domain.model.gateway;

import com.reservasapp.canchas.domain.model.Bloqueo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BloqueoGateway {
    Bloqueo guardar(Bloqueo bloqueo);

    Optional<Bloqueo> buscarPorId(Long id);

    List<Bloqueo> listarSolapados(Long canchaId, LocalDateTime desde, LocalDateTime hasta);

    void eliminar(Long id);

    void eliminarPorCancha(Long canchaId);
}
