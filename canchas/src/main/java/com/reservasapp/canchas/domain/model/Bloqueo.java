package com.reservasapp.canchas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bloqueo {
    private Long id;
    private Long canchaId;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String motivo;
}
