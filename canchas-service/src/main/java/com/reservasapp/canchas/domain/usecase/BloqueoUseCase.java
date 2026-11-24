package com.reservasapp.canchas.domain.usecase;

import com.reservasapp.canchas.domain.exception.NegocioException;
import com.reservasapp.canchas.domain.model.Bloqueo;
import com.reservasapp.canchas.domain.model.gateway.BloqueoGateway;
import com.reservasapp.canchas.domain.model.gateway.CanchaGateway;

import java.time.LocalDateTime;

public class BloqueoUseCase {

    private final CanchaGateway canchaGateway;
    private final BloqueoGateway bloqueoGateway;

    public BloqueoUseCase(CanchaGateway canchaGateway, BloqueoGateway bloqueoGateway) {
        this.canchaGateway = canchaGateway;
        this.bloqueoGateway = bloqueoGateway;
    }

    public Bloqueo crear(Long canchaId, LocalDateTime fechaInicio, LocalDateTime fechaFin, String motivo) {
        if (!canchaGateway.existePorId(canchaId)) {
            throw NegocioException.canchaNoEncontrada();
        }
        if (!enPunto(fechaInicio) || !enPunto(fechaFin) || !fechaFin.isAfter(fechaInicio)) {
            throw NegocioException.horarioInvalido(
                    "El bloqueo debe empezar y terminar en hora en punto, y el fin debe ser posterior al inicio");
        }
        return bloqueoGateway.guardar(Bloqueo.builder()
                .canchaId(canchaId)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .motivo(motivo)
                .build());
    }

    public void eliminar(Long canchaId, Long bloqueoId) {
        bloqueoGateway.buscarPorId(bloqueoId)
                .filter(bloqueo -> bloqueo.getCanchaId().equals(canchaId))
                .orElseThrow(NegocioException::bloqueoNoEncontrado);
        bloqueoGateway.eliminar(bloqueoId);
    }

    private boolean enPunto(LocalDateTime fechaHora) {
        return fechaHora.getMinute() == 0 && fechaHora.getSecond() == 0 && fechaHora.getNano() == 0;
    }
}
