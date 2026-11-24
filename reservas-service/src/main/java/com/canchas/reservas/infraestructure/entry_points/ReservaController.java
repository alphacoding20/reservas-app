package com.canchas.reservas.infraestructure.entry_points;

import com.canchas.reservas.domain.model.MotivoCancelacion;
import com.canchas.reservas.domain.model.Reserva;
import com.canchas.reservas.domain.usecase.DisponibilidadUseCase;
import com.canchas.reservas.domain.usecase.EstadisticasUseCase;
import com.canchas.reservas.domain.usecase.PronosticoUseCase;
import com.canchas.reservas.domain.usecase.RecordatorioUseCase;
import com.canchas.reservas.domain.usecase.ReservaUseCase;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.AlertaLluviaDTO;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.AsistenciaRequest;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.CrearReservaRequest;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.DisponibilidadDTO;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.EstadisticasDTO;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.PronosticoDTO;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.RecordatoriosDTO;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.ReprogramarRequest;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.ReservaDTO;
import com.canchas.reservas.infraestructure.mapper.ReservaDtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

    static final String HEADER_USUARIO = "X-Usuario-Id";

    private final ReservaUseCase reservaUseCase;
    private final DisponibilidadUseCase disponibilidadUseCase;
    private final PronosticoUseCase pronosticoUseCase;
    private final RecordatorioUseCase recordatorioUseCase;
    private final EstadisticasUseCase estadisticasUseCase;
    private final ReservaDtoMapper mapper;

    // ------------------------------------------------------------------ CRUD

    @PostMapping
    public ResponseEntity<ReservaDTO> crear(@Valid @RequestBody CrearReservaRequest request) {
        Reserva reserva = reservaUseCase.crear(request.canchaId(), request.usuarioId(), request.fecha(),
                request.horaInicio(), request.horaFin());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(reserva));
    }

    @GetMapping
    public List<ReservaDTO> listar() {
        return mapper.toDtos(reservaUseCase.listarTodas());
    }

    @GetMapping("/{id}")
    public ReservaDTO obtener(@PathVariable Long id) {
        return mapper.toDto(reservaUseCase.obtener(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reservaUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------- no CRUD

    @GetMapping("/disponibilidad")
    public DisponibilidadDTO disponibilidad(
            @RequestParam Long canchaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return mapper.toDto(disponibilidadUseCase.consultar(canchaId, fecha));
    }

    @PatchMapping("/{id}/cancelar")
    public ReservaDTO cancelar(@PathVariable Long id,
                               @RequestParam(defaultValue = "USUARIO") MotivoCancelacion motivo) {
        return mapper.toDto(reservaUseCase.cancelar(id, motivo));
    }

    @PatchMapping("/{id}/reprogramar")
    public ReservaDTO reprogramar(@PathVariable Long id, @Valid @RequestBody ReprogramarRequest request) {
        return mapper.toDto(reservaUseCase.reprogramar(id, request.fecha(), request.horaInicio(), request.horaFin()));
    }

    @GetMapping("/{id}/pronostico")
    public PronosticoDTO pronostico(@PathVariable Long id) {
        return mapper.toDto(pronosticoUseCase.pronosticoDeReserva(id));
    }

    @PostMapping("/alertas/lluvia")
    public AlertaLluviaDTO alertasLluvia(
            @RequestHeader(value = HEADER_USUARIO, required = false) Long adminId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return mapper.toDto(pronosticoUseCase.alertarLluvia(adminId, fecha));
    }

    @PostMapping("/recordatorios")
    public RecordatoriosDTO recordatorios(
            @RequestHeader(value = HEADER_USUARIO, required = false) Long adminId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return mapper.toDto(recordatorioUseCase.enviarManual(adminId, fecha));
    }

    @PatchMapping("/{id}/asistencia")
    public ReservaDTO asistencia(
            @RequestHeader(value = HEADER_USUARIO, required = false) Long adminId,
            @PathVariable Long id,
            @Valid @RequestBody AsistenciaRequest request) {
        return mapper.toDto(reservaUseCase.registrarAsistencia(adminId, id, request.estado()));
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<ReservaDTO> historial(@PathVariable Long usuarioId) {
        return mapper.toDtos(reservaUseCase.historialDeUsuario(usuarioId));
    }

    @GetMapping("/cancha/{canchaId}/agenda")
    public List<ReservaDTO> agenda(
            @PathVariable Long canchaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return mapper.toDtos(reservaUseCase.agendaDeCancha(canchaId, fecha));
    }

    @GetMapping("/estadisticas")
    public EstadisticasDTO estadisticas(
            @RequestHeader(value = HEADER_USUARIO, required = false) Long adminId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return mapper.toDto(estadisticasUseCase.consultar(adminId, desde, hasta));
    }
}
