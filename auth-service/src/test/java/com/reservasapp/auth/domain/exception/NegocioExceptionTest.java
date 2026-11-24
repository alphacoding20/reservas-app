package com.reservasapp.auth.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NegocioExceptionTest {

    private void verificar(NegocioException ex, int status, String codigo) {
        assertEquals(status, ex.getStatus());
        assertEquals(codigo, ex.getCodigo());
    }

    @Test
    void cadaFabricaUsaElStatusYCodigoDelContrato() {
        verificar(NegocioException.usuarioNoEncontrado(), 404, "USUARIO_NO_ENCONTRADO");
        verificar(NegocioException.credencialesInvalidas(), 401, "CREDENCIALES_INVALIDAS");
        verificar(NegocioException.correoDuplicado(), 409, "CORREO_DUPLICADO");
        verificar(NegocioException.claveIncorrecta(), 400, "CLAVE_INCORRECTA");
        verificar(NegocioException.usuarioInactivo(), 403, "USUARIO_INACTIVO");
        verificar(NegocioException.sinHeader(), 401, "NO_AUTORIZADO");
        verificar(NegocioException.sinPermiso(), 403, "NO_AUTORIZADO");
    }

    @Test
    void elMensajeNoEsVacio() {
        assertEquals("El usuario no existe", NegocioException.usuarioNoEncontrado().getMessage());
    }
}
