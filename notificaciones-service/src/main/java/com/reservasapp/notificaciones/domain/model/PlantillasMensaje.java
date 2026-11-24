package com.reservasapp.notificaciones.domain.model;

import lombok.experimental.UtilityClass;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


@UtilityClass
public class PlantillasMensaje {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public static String generar(TipoNotificacion tipo, SolicitudNotificacion s) {
        return switch (tipo) {
            case CONFIRMACION -> """
                    ✅ Reserva confirmada

                    Hola %s, tu reserva #%d en %s quedó confirmada.
                    📅 %s, de %s a %s
                    💰 Total: %s"""
                    .formatted(s.nombreUsuario(), s.reservaId(), s.nombreCancha(),
                            fecha(s), horaInicio(s), horaFin(s), dinero(s.precioTotal()));
            case RECORDATORIO -> """
                    ⏰ Recordatorio de tu reserva

                    Hola %s, te recordamos tu reserva #%d en %s.
                    📅 %s, de %s a %s
                    ¡Te esperamos!"""
                    .formatted(s.nombreUsuario(), s.reservaId(), s.nombreCancha(),
                            fecha(s), horaInicio(s), horaFin(s));
            case CANCELACION -> """
                    ❌ Reserva cancelada

                    Hola %s, %s: reserva #%d en %s.
                    📅 %s, de %s a %s"""
                    .formatted(s.nombreUsuario(), textoMotivo(s.motivo()), s.reservaId(), s.nombreCancha(),
                            fecha(s), horaInicio(s), horaFin(s));
            case REPROGRAMACION -> """
                    🔄 Reserva reprogramada

                    Hola %s, tu reserva #%d en %s cambió.
                    Antes: %s, desde las %s
                    Ahora: %s, de %s a %s"""
                    .formatted(s.nombreUsuario(), s.reservaId(), s.nombreCancha(),
                            s.fechaAnterior().format(FECHA), s.horaInicioAnterior().format(HORA),
                            fecha(s), horaInicio(s), horaFin(s));
            case ALERTA_LLUVIA -> """
                    🌧️ Alerta de lluvia

                    Hola %s, hay %d%% de probabilidad de lluvia para tu reserva #%d en %s.
                    📅 %s, de %s a %s
                    Si quieres, puedes cancelarla indicando la lluvia como motivo."""
                    .formatted(s.nombreUsuario(), s.probabilidadLluvia(), s.reservaId(), s.nombreCancha(),
                            fecha(s), horaInicio(s), horaFin(s));
        };
    }

    private static String textoMotivo(MotivoCancelacion motivo) {
        return switch (motivo) {
            case USUARIO -> "cancelaste tu reserva";
            case LLUVIA -> "tu reserva fue cancelada por pronóstico de lluvia";
            case ADMIN -> "tu reserva fue cancelada por la administración";
        };
    }

    private static String fecha(SolicitudNotificacion s) {
        return s.fecha().format(FECHA);
    }

    private static String horaInicio(SolicitudNotificacion s) {
        return s.horaInicio().format(HORA);
    }

    private static String horaFin(SolicitudNotificacion s) {
        return s.horaFin().format(HORA);
    }

    static String dinero(Long valor) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
        simbolos.setGroupingSeparator('.');
        return "$" + new DecimalFormat("#,##0", simbolos).format(valor) + " COP";
    }
}
