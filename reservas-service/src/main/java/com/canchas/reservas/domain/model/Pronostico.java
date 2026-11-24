package com.canchas.reservas.domain.model;

public record Pronostico(Long reservaId, boolean aplica, boolean pronosticoDisponible, Integer probabilidadLluvia,
                         boolean riesgoLluvia, int umbral) {
}
