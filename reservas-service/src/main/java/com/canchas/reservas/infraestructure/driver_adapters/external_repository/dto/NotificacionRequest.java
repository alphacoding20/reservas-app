package com.canchas.reservas.infraestructure.driver_adapters.external_repository.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificacionRequest(Long usuarioId, String nombreUsuario, String canal, String destinatario,
                                  Long reservaId, String nombreCancha, String fecha, String horaInicio,
                                  String horaFin, Long precioTotal, String motivo, String fechaAnterior,
                                  String horaInicioAnterior, Integer probabilidadLluvia) {
}
