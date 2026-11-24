package com.canchas.reservas.domain.model.gateway;

import com.canchas.reservas.domain.model.DatosNotificacion;
import com.canchas.reservas.domain.model.MotivoCancelacion;

import java.time.LocalDate;
import java.time.LocalTime;

public interface NotificacionServiceGateway {

    void enviarConfirmacion(DatosNotificacion datos);

    void enviarRecordatorio(DatosNotificacion datos);

    void enviarCancelacion(DatosNotificacion datos, MotivoCancelacion motivo);

    void enviarReprogramacion(DatosNotificacion datos, LocalDate fechaAnterior, LocalTime horaInicioAnterior);

    void enviarAlertaLluvia(DatosNotificacion datos, int probabilidadLluvia);
}
