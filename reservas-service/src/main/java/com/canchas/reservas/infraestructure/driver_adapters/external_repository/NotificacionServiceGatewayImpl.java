package com.canchas.reservas.infraestructure.driver_adapters.external_repository;

import com.canchas.reservas.domain.model.DatosNotificacion;
import com.canchas.reservas.domain.model.MotivoCancelacion;
import com.canchas.reservas.domain.model.gateway.NotificacionServiceGateway;
import com.canchas.reservas.infraestructure.driver_adapters.external_repository.dto.NotificacionRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class NotificacionServiceGatewayImpl implements NotificacionServiceGateway {

    private static final Logger LOG = Logger.getLogger(NotificacionServiceGatewayImpl.class.getName());
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    static final String RUTA_CONFIRMACION = "confirmacion";
    static final String RUTA_RECORDATORIO = "recordatorio";
    static final String RUTA_CANCELACION = "cancelacion";
    static final String RUTA_REPROGRAMACION = "reprogramacion";
    /** Con guion, tal como la expone notificaciones-service (contrato 6.3). */
    static final String RUTA_ALERTA_LLUVIA = "alerta-lluvia";

    private final RestClient client;

    public NotificacionServiceGatewayImpl(@Qualifier("notificacionesRestClient") RestClient client) {
        this.client = client;
    }

    @Async("notificacionExecutor")
    @Override
    public void enviarConfirmacion(DatosNotificacion datos) {
        enviar(RUTA_CONFIRMACION, base(datos, null, null, null, null));
    }

    @Async("notificacionExecutor")
    @Override
    public void enviarRecordatorio(DatosNotificacion datos) {
        enviar(RUTA_RECORDATORIO, base(datos, null, null, null, null));
    }

    @Async("notificacionExecutor")
    @Override
    public void enviarCancelacion(DatosNotificacion datos, MotivoCancelacion motivo) {
        enviar(RUTA_CANCELACION, base(datos, motivo.name(), null, null, null));
    }

    @Async("notificacionExecutor")
    @Override
    public void enviarReprogramacion(DatosNotificacion datos, LocalDate fechaAnterior, LocalTime horaInicioAnterior) {
        enviar(RUTA_REPROGRAMACION,
                base(datos, null, fechaAnterior.toString(), horaInicioAnterior.format(HORA), null));
    }

    @Async("notificacionExecutor")
    @Override
    public void enviarAlertaLluvia(DatosNotificacion datos, int probabilidadLluvia) {
        enviar(RUTA_ALERTA_LLUVIA, base(datos, null, null, null, probabilidadLluvia));
    }

    private NotificacionRequest base(DatosNotificacion d, String motivo, String fechaAnterior,
                                     String horaInicioAnterior, Integer probabilidadLluvia) {
        return new NotificacionRequest(d.usuarioId(), d.nombreUsuario(), d.canal().name(), d.destinatario(),
                d.reservaId(), d.nombreCancha(), d.fecha().toString(), d.horaInicio().format(HORA),
                d.horaFin().format(HORA), d.precioTotal(), motivo, fechaAnterior, horaInicioAnterior,
                probabilidadLluvia);
    }

    private void enviar(String ruta, NotificacionRequest cuerpo) {
        try {
            client.post()
                    .uri("/api/notificaciones/{tipo}", ruta)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "notificaciones-service no recibió '" + ruta + "' de la reserva "
                    + cuerpo.reservaId() + ": " + e.getMessage());
        }
    }
}
