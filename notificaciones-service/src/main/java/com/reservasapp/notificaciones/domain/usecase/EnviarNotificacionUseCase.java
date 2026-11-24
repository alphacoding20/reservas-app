package com.reservasapp.notificaciones.domain.usecase;

import com.reservasapp.notificaciones.domain.model.EstadoNotificacion;
import com.reservasapp.notificaciones.domain.model.Notificacion;
import com.reservasapp.notificaciones.domain.model.PlantillasMensaje;
import com.reservasapp.notificaciones.domain.model.SolicitudNotificacion;
import com.reservasapp.notificaciones.domain.model.TipoNotificacion;
import com.reservasapp.notificaciones.domain.model.ValidacionException;
import com.reservasapp.notificaciones.domain.model.gateway.NotificacionGateway;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Los 5 endpoints de envio (confirmacion, recordatorio, cancelacion, reprogramacion, alerta-lluvia)
 * comparten esta logica; solo cambian el tipo, la plantilla y el campo extra obligatorio.
 */
@RequiredArgsConstructor
public class EnviarNotificacionUseCase {

    private final NotificacionGateway notificacionGateway;
    private final DespachadorEnvio despachador;
    private final Clock clock;

    public Notificacion ejecutar(TipoNotificacion tipo, SolicitudNotificacion solicitud) {
        validar(tipo, solicitud);

        String mensaje = PlantillasMensaje.generar(tipo, solicitud);
        EstadoNotificacion estado = despachador.despachar(solicitud.canal(), solicitud.destinatario(), mensaje);

        Notificacion notificacion = Notificacion.builder().usuarioId(solicitud.usuarioId()).canal(solicitud.canal()).destinatario(solicitud.destinatario()).tipo(tipo).mensaje(mensaje).estado(estado).intentos(1).fecha(LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS)).build();

        // Se guarda siempre, incluso FALLIDA: el llamador recibe 201 y no se entera del fallo de Telegram.
        return notificacionGateway.guardar(notificacion);
    }

    private void validar(TipoNotificacion tipo, SolicitudNotificacion s) {
        if (tipo == null || s == null) {
            throw new ValidacionException("El cuerpo de la petición es obligatorio");
        }

        List<String> faltantes = new ArrayList<>();
        requerido(s.usuarioId(), "usuarioId", faltantes);
        requerido(s.nombreUsuario(), "nombreUsuario", faltantes);
        requerido(s.canal(), "canal", faltantes);
        requerido(s.destinatario(), "destinatario", faltantes);
        requerido(s.reservaId(), "reservaId", faltantes);
        requerido(s.nombreCancha(), "nombreCancha", faltantes);
        requerido(s.fecha(), "fecha", faltantes);
        requerido(s.horaInicio(), "horaInicio", faltantes);
        requerido(s.horaFin(), "horaFin", faltantes);

        switch (tipo) {
            case CONFIRMACION -> requerido(s.precioTotal(), "precioTotal", faltantes);
            case CANCELACION -> requerido(s.motivo(), "motivo", faltantes);
            case REPROGRAMACION -> {
                requerido(s.fechaAnterior(), "fechaAnterior", faltantes);
                requerido(s.horaInicioAnterior(), "horaInicioAnterior", faltantes);
            }
            case ALERTA_LLUVIA -> requerido(s.probabilidadLluvia(), "probabilidadLluvia", faltantes);
            case RECORDATORIO -> {
            }
        }

        if (!faltantes.isEmpty()) {
            throw new ValidacionException("Campos obligatorios faltantes: " + String.join(", ", faltantes));
        }

        if (tipo == TipoNotificacion.ALERTA_LLUVIA && (s.probabilidadLluvia() < 0 || s.probabilidadLluvia() > 100)) {
            throw new ValidacionException("probabilidadLluvia debe estar entre 0 y 100");
        }
    }

    private static void requerido(Object valor, String campo, List<String> faltantes) {
        if (valor == null || (valor instanceof String texto && texto.isBlank())) {
            faltantes.add(campo);
        }
    }
}
