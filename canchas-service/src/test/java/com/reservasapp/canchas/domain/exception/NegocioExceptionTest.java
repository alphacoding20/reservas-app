package com.reservasapp.canchas.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NegocioExceptionTest {

    private void verificar(NegocioException ex, int status, String codigo) {
        assertEquals(status, ex.getStatus());
        assertEquals(codigo, ex.getCodigo());
    }

    @Test
    void cadaFabricaUsaElStatusYCodigoDelContrato() {
        verificar(NegocioException.canchaNoEncontrada(), 404, "CANCHA_NO_ENCONTRADA");
        verificar(NegocioException.bloqueoNoEncontrado(), 404, "BLOQUEO_NO_ENCONTRADO");
        verificar(NegocioException.horarioInvalido("x"), 400, "HORARIO_INVALIDO");
        verificar(NegocioException.sinHeader(), 401, "NO_AUTORIZADO");
        verificar(NegocioException.sinPermiso(), 403, "NO_AUTORIZADO");
        verificar(NegocioException.servicioNoDisponible(), 502, "SERVICIO_NO_DISPONIBLE");
    }

    @Test
    void horarioInvalidoConservaElMensaje() {
        assertEquals("detalle", NegocioException.horarioInvalido("detalle").getMessage());
    }
}
