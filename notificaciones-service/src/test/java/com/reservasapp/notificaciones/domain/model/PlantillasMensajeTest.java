package com.reservasapp.notificaciones.domain.model;

import com.reservasapp.notificaciones.TestData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlantillasMensajeTest {

    private final SolicitudNotificacion solicitud = TestData.solicitudCompleta();

    @Test
    void confirmacionIncluyeDatosDeLaReservaYElPrecioConPuntoDeMiles() {
        String mensaje = PlantillasMensaje.generar(TipoNotificacion.CONFIRMACION, solicitud);

        assertThat(mensaje)
                .contains("Ana Pérez", "#55", "Sintética 1", "08/12/2026", "18:00", "19:00")
                .contains("$60.000 COP");
    }

    @Test
    void recordatorioIncluyeFranjaYCancha() {
        String mensaje = PlantillasMensaje.generar(TipoNotificacion.RECORDATORIO, solicitud);

        assertThat(mensaje).contains("Recordatorio", "Ana Pérez", "#55", "Sintética 1", "08/12/2026");
    }

    @Test
    void cancelacionCambiaElTextoSegunElMotivo() {
        String usuario = PlantillasMensaje.generar(TipoNotificacion.CANCELACION, conMotivo(MotivoCancelacion.USUARIO));
        String lluvia = PlantillasMensaje.generar(TipoNotificacion.CANCELACION, conMotivo(MotivoCancelacion.LLUVIA));
        String admin = PlantillasMensaje.generar(TipoNotificacion.CANCELACION, conMotivo(MotivoCancelacion.ADMIN));

        assertThat(usuario).contains("cancelaste tu reserva");
        assertThat(lluvia).contains("pronóstico de lluvia");
        assertThat(admin).contains("administración");
    }

    @Test
    void reprogramacionMuestraLaFranjaAnteriorYLaNueva() {
        String mensaje = PlantillasMensaje.generar(TipoNotificacion.REPROGRAMACION, solicitud);

        assertThat(mensaje).contains("Antes: 07/12/2026", "17:00").contains("Ahora: 08/12/2026", "18:00", "19:00");
    }

    @Test
    void alertaLluviaMuestraLaProbabilidad() {
        String mensaje = PlantillasMensaje.generar(TipoNotificacion.ALERTA_LLUVIA, solicitud);

        assertThat(mensaje).contains("70% de probabilidad", "#55", "Sintética 1");
    }

    @Test
    void dineroUsaPuntoComoSeparadorDeMiles() {
        assertThat(PlantillasMensaje.dinero(0L)).isEqualTo("$0 COP");
        assertThat(PlantillasMensaje.dinero(950L)).isEqualTo("$950 COP");
        assertThat(PlantillasMensaje.dinero(1250000L)).isEqualTo("$1.250.000 COP");
    }

    private SolicitudNotificacion conMotivo(MotivoCancelacion motivo) {
        return new SolicitudNotificacion(solicitud.usuarioId(), solicitud.nombreUsuario(), solicitud.canal(),
                solicitud.destinatario(), solicitud.reservaId(), solicitud.nombreCancha(), solicitud.fecha(),
                solicitud.horaInicio(), solicitud.horaFin(), solicitud.precioTotal(), motivo,
                null, null, null);
    }
}
