package com.reservasapp.notificaciones.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    private Long id;
    private Long usuarioId;
    private Canal canal;
    private String destinatario;
    private TipoNotificacion tipo;
    private String mensaje;
    private EstadoNotificacion estado;
    private int intentos;
    private LocalDateTime fecha;
}
