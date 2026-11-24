package com.reservasapp.notificaciones.infraestructure.entry_points;

import com.reservasapp.notificaciones.domain.model.TipoNotificacion;
import com.reservasapp.notificaciones.domain.usecase.ConsultarNotificacionesUseCase;
import com.reservasapp.notificaciones.domain.usecase.EnviarNotificacionUseCase;
import com.reservasapp.notificaciones.domain.usecase.EstadisticasNotificacionesUseCase;
import com.reservasapp.notificaciones.domain.usecase.ReenviarNotificacionUseCase;
import com.reservasapp.notificaciones.infraestructure.entry_points.dto.EstadisticasDTO;
import com.reservasapp.notificaciones.infraestructure.entry_points.dto.NotificacionDTO;
import com.reservasapp.notificaciones.infraestructure.entry_points.dto.SolicitudNotificacionRequest;
import com.reservasapp.notificaciones.infraestructure.mapper.NotificacionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Envío por Telegram, reintentos, historial y estadísticas")
public class NotificacionController {

    private final EnviarNotificacionUseCase enviarUseCase;
    private final ReenviarNotificacionUseCase reenviarUseCase;
    private final ConsultarNotificacionesUseCase consultarUseCase;
    private final EstadisticasNotificacionesUseCase estadisticasUseCase;
    private final NotificacionMapper mapper;

    // ---- 5 endpoints de envio: responden 201 aunque Telegram falle (estado=FALLIDA) ----

    @PostMapping("/confirmacion")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Envía la confirmación de una reserva (requiere precioTotal)")
    public NotificacionDTO confirmacion(@RequestBody SolicitudNotificacionRequest request) {
        return enviar(TipoNotificacion.CONFIRMACION, request);
    }

    @PostMapping("/recordatorio")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Envía el recordatorio previo a la reserva")
    public NotificacionDTO recordatorio(@RequestBody SolicitudNotificacionRequest request) {
        return enviar(TipoNotificacion.RECORDATORIO, request);
    }

    @PostMapping("/cancelacion")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Avisa una cancelación (requiere motivo: USUARIO, LLUVIA o ADMIN)")
    public NotificacionDTO cancelacion(@RequestBody SolicitudNotificacionRequest request) {
        return enviar(TipoNotificacion.CANCELACION, request);
    }

    @PostMapping("/reprogramacion")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Avisa una reprogramación (requiere fechaAnterior y horaInicioAnterior)")
    public NotificacionDTO reprogramacion(@RequestBody SolicitudNotificacionRequest request) {
        return enviar(TipoNotificacion.REPROGRAMACION, request);
    }

    @PostMapping("/alerta-lluvia")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Envía la alerta por pronóstico de lluvia (requiere probabilidadLluvia 0-100)")
    public NotificacionDTO alertaLluvia(@RequestBody SolicitudNotificacionRequest request) {
        return enviar(TipoNotificacion.ALERTA_LLUVIA, request);
    }

    // ---- gestion ----

    @PostMapping("/{id}/reenviar")
    @Operation(summary = "Reintenta una notificación FALLIDA (intentos + 1)")
    public NotificacionDTO reenviar(@PathVariable Long id) {
        return mapper.toDto(reenviarUseCase.ejecutar(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Historial de notificaciones del usuario, más recientes primero")
    public List<NotificacionDTO> porUsuario(@PathVariable Long usuarioId) {
        return consultarUseCase.porUsuario(usuarioId).stream().map(mapper::toDto).toList();
    }

    @GetMapping("/estadisticas")
    @Operation(summary = "Enviadas frente a fallidas, total y por tipo")
    public EstadisticasDTO estadisticas() {
        return mapper.toDto(estadisticasUseCase.ejecutar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta una notificación por id")
    public NotificacionDTO porId(@PathVariable Long id) {
        return mapper.toDto(consultarUseCase.porId(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina una notificación")
    public void eliminar(@PathVariable Long id) {
        consultarUseCase.eliminar(id);
    }

    private NotificacionDTO enviar(TipoNotificacion tipo, SolicitudNotificacionRequest request) {
        return mapper.toDto(enviarUseCase.ejecutar(tipo, mapper.toSolicitud(request)));
    }
}
