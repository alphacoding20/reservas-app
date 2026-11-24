package com.canchas.reservas.domain.model;

public record CanchaInfo(Long id, String nombre, Double latitud, Double longitud, boolean techada,
                         EstadoCancha estado) {

    public boolean tieneCoordenadas() {
        return latitud != null && longitud != null;
    }
}
