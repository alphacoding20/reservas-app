package com.canchas.reservas.infraestructure.entry_points.dto;

import com.canchas.reservas.domain.model.EstadoReserva;
import com.canchas.reservas.domain.model.MotivoCancelacion;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Cuerpos de petición y respuesta de la API, tal como los define el contrato (claves en camelCase).
 * Formatos fijos: fecha yyyy-MM-dd, hora HH:mm, fecha-hora ISO sin zona.
 */
public final class ReservaDtos {

    private static final String FECHA = "yyyy-MM-dd";
    private static final String HORA = "HH:mm";
    private static final String FECHA_HORA = "yyyy-MM-dd'T'HH:mm:ss";

    private ReservaDtos() {
    }

    // ------------------------------------------------------------ peticiones

    public record CrearReservaRequest(
            @NotNull Long canchaId,
            @NotNull Long usuarioId,
            @NotNull @JsonFormat(pattern = FECHA) LocalDate fecha,
            @NotNull @JsonFormat(pattern = HORA) LocalTime horaInicio,
            @NotNull @JsonFormat(pattern = HORA) LocalTime horaFin) {
    }

    public record ReprogramarRequest(
            @NotNull @JsonFormat(pattern = FECHA) LocalDate fecha,
            @NotNull @JsonFormat(pattern = HORA) LocalTime horaInicio,
            @NotNull @JsonFormat(pattern = HORA) LocalTime horaFin) {
    }

    public record AsistenciaRequest(@NotNull EstadoReserva estado) {
    }

    // ------------------------------------------------------------ respuestas

    public record ReservaDTO(
            Long id,
            Long canchaId,
            Long usuarioId,
            @JsonFormat(pattern = FECHA) LocalDate fecha,
            @JsonFormat(pattern = HORA) LocalTime horaInicio,
            @JsonFormat(pattern = HORA) LocalTime horaFin,
            Long precioTotal,
            EstadoReserva estado,
            MotivoCancelacion motivoCancelacion,
            @JsonFormat(pattern = FECHA_HORA) LocalDateTime fechaCreacion) {
    }

    public record FranjaDTO(
            @JsonFormat(pattern = HORA) LocalTime horaInicio,
            @JsonFormat(pattern = HORA) LocalTime horaFin,
            boolean disponible) {
    }

    public record DisponibilidadDTO(
            Long canchaId,
            @JsonFormat(pattern = FECHA) LocalDate fecha,
            List<FranjaDTO> franjas) {
    }

    public record PronosticoDTO(
            Long reservaId,
            boolean aplica,
            boolean pronosticoDisponible,
            Integer probabilidadLluvia,
            boolean riesgoLluvia,
            int umbral) {
    }

    public record AlertaLluviaDTO(
            @JsonFormat(pattern = FECHA) LocalDate fecha,
            int reservasRevisadas,
            int alertasEnviadas,
            int sinPronostico) {
    }

    public record RecordatoriosDTO(
            @JsonFormat(pattern = FECHA) LocalDate fecha,
            int recordatoriosEnviados) {
    }

    public record OcupacionCanchaDTO(Long canchaId, long horasReservadas) {
    }

    public record EstadisticasDTO(
            @JsonFormat(pattern = FECHA) LocalDate desde,
            @JsonFormat(pattern = FECHA) LocalDate hasta,
            long totalReservas,
            Map<String, Long> porEstado,
            List<OcupacionCanchaDTO> ocupacionPorCancha) {
    }

    /** Formato único de error de todos los servicios (contrato 2.4). */
    public record ErrorDTO(
            @JsonFormat(pattern = FECHA_HORA) LocalDateTime timestamp,
            int status,
            String error,
            String mensaje,
            String ruta) {
    }
}
