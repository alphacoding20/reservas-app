package com.canchas.reservas.domain.usecase;

import com.canchas.reservas.domain.model.CanchaInfo;
import com.canchas.reservas.domain.model.Canal;
import com.canchas.reservas.domain.model.DatosNotificacion;
import com.canchas.reservas.domain.model.MotivoCancelacion;
import com.canchas.reservas.domain.model.Reserva;
import com.canchas.reservas.domain.model.UsuarioInfo;
import com.canchas.reservas.domain.model.gateway.NotificacionServiceGateway;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotificadorReserva {

    private static final Logger LOG = Logger.getLogger(NotificadorReserva.class.getName());

    private final NotificacionServiceGateway gateway;

    public NotificadorReserva(NotificacionServiceGateway gateway) {
        this.gateway = gateway;
    }

    public void confirmacion(Reserva reserva, UsuarioInfo usuario, CanchaInfo cancha) {
        enviar("confirmacion", () -> gateway.enviarConfirmacion(datos(reserva, usuario, cancha)));
    }

    public void recordatorio(Reserva reserva, UsuarioInfo usuario, CanchaInfo cancha) {
        enviar("recordatorio", () -> gateway.enviarRecordatorio(datos(reserva, usuario, cancha)));
    }

    public void cancelacion(Reserva reserva, UsuarioInfo usuario, CanchaInfo cancha, MotivoCancelacion motivo) {
        enviar("cancelacion", () -> gateway.enviarCancelacion(datos(reserva, usuario, cancha), motivo));
    }

    public void reprogramacion(Reserva reserva, UsuarioInfo usuario, CanchaInfo cancha, LocalDate fechaAnterior,
                               LocalTime horaInicioAnterior) {
        enviar("reprogramacion", () ->
                gateway.enviarReprogramacion(datos(reserva, usuario, cancha), fechaAnterior, horaInicioAnterior));
    }

    public void alertaLluvia(Reserva reserva, UsuarioInfo usuario, CanchaInfo cancha, int probabilidadLluvia) {
        enviar("alerta-lluvia", () ->
                gateway.enviarAlertaLluvia(datos(reserva, usuario, cancha), probabilidadLluvia));
    }

    static DatosNotificacion datos(Reserva reserva, UsuarioInfo usuario, CanchaInfo cancha) {
        Canal canal = usuario.tieneTelegram() ? Canal.TELEGRAM : Canal.EMAIL;
        String destinatario = usuario.tieneTelegram() ? usuario.telegramChatId() : usuario.correo();
        return new DatosNotificacion(usuario.id(), usuario.nombre(), canal, destinatario, reserva.id(),
                cancha.nombre(), reserva.fecha(), reserva.horaInicio(), reserva.horaFin(), reserva.precioTotal());
    }

    private void enviar(String tipo, Runnable envio) {
        try {
            envio.run();
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo enviar la notificación de " + tipo + ": " + e.getMessage(), e);
        }
    }
}
