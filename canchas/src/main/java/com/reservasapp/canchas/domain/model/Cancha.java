package com.reservasapp.canchas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cancha {
    private Long id;
    private String nombre;
    private TipoDeporte tipoDeporte;
    private String direccion;
    private Double latitud;
    private Double longitud;
    private Long precioPorHora;
    private boolean techada;
    private LocalTime horaApertura;
    private LocalTime horaCierre;
    private EstadoCancha estado;
}
