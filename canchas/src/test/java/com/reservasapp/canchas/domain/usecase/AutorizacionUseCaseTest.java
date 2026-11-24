package com.reservasapp.canchas.domain.usecase;

import com.reservasapp.canchas.domain.exception.NegocioException;
import com.reservasapp.canchas.domain.model.UsuarioInfo;
import com.reservasapp.canchas.domain.model.gateway.AuthGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutorizacionUseCaseTest {

    @Mock
    private AuthGateway authGateway;
    @InjectMocks
    private AutorizacionUseCase useCase;

    @Test
    void exigirAdmin_sinHeader_lanza401() {
        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.exigirAdmin(null));

        assertEquals(401, ex.getStatus());
        assertEquals("NO_AUTORIZADO", ex.getCodigo());
        verifyNoInteractions(authGateway);
    }

    @Test
    void exigirAdmin_usuarioNoExiste_lanza403() {
        when(authGateway.validar(9L)).thenReturn(Optional.empty());

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.exigirAdmin(9L));

        assertEquals(403, ex.getStatus());
    }

    @Test
    void exigirAdmin_rolUsuario_lanza403() {
        when(authGateway.validar(2L)).thenReturn(Optional.of(new UsuarioInfo(2L, "USUARIO", true)));

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.exigirAdmin(2L));

        assertEquals(403, ex.getStatus());
    }

    @Test
    void exigirAdmin_adminInactivo_lanza403() {
        when(authGateway.validar(1L)).thenReturn(Optional.of(new UsuarioInfo(1L, "ADMIN", false)));

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.exigirAdmin(1L));

        assertEquals(403, ex.getStatus());
    }

    @Test
    void exigirAdmin_adminActivo_noLanzaNada() {
        when(authGateway.validar(1L)).thenReturn(Optional.of(new UsuarioInfo(1L, "ADMIN", true)));

        assertDoesNotThrow(() -> useCase.exigirAdmin(1L));
    }

    @Test
    void exigirAdmin_authCaido_propaga502() {
        when(authGateway.validar(1L)).thenThrow(NegocioException.servicioNoDisponible());

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.exigirAdmin(1L));

        assertEquals(502, ex.getStatus());
        assertEquals("SERVICIO_NO_DISPONIBLE", ex.getCodigo());
    }
}
