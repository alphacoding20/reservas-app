package com.canchas.reservas.domain.usecase;

import com.canchas.reservas.domain.exception.CodigoError;
import com.canchas.reservas.domain.exception.ReservasException;
import com.canchas.reservas.domain.model.Rol;
import com.canchas.reservas.domain.model.UsuarioInfo;
import com.canchas.reservas.domain.model.gateway.UsuarioServiceGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static com.canchas.reservas.Fixtures.admin;
import static com.canchas.reservas.Fixtures.assertCodigo;
import static com.canchas.reservas.Fixtures.usuario;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ValidadorAdminTest {

    @Mock
    UsuarioServiceGateway usuarios;

    ValidadorAdmin validador;

    @BeforeEach
    void preparar() {
        validador = new ValidadorAdmin(usuarios);
    }

    @Test
    void sinHeaderEs401() {
        assertCodigo(CodigoError.SIN_CREDENCIALES, () -> validador.exigirAdmin(null));
        verifyNoInteractions(usuarios);
    }

    @Test
    void adminActivoPasa() {
        when(usuarios.validar(1L)).thenReturn(admin());
        assertDoesNotThrow(() -> validador.exigirAdmin(1L));
    }

    @Test
    void usuarioSinRolAdminEs403() {
        when(usuarios.validar(2L)).thenReturn(usuario());
        assertCodigo(CodigoError.PERMISO_DENEGADO, () -> validador.exigirAdmin(2L));
    }

    @Test
    void adminInactivoEs403() {
        when(usuarios.validar(1L)).thenReturn(new UsuarioInfo(1L, "Admin", "a@mail.com", Rol.ADMIN, false, null));
        assertCodigo(CodigoError.PERMISO_DENEGADO, () -> validador.exigirAdmin(1L));
    }

    @Test
    void usuarioInexistenteSeTrataComoSinPermiso() {
        when(usuarios.validar(99L)).thenThrow(new ReservasException(CodigoError.USUARIO_NO_ENCONTRADO, "no existe"));
        assertCodigo(CodigoError.PERMISO_DENEGADO, () -> validador.exigirAdmin(99L));
    }

    @Test
    void otrosFallosDeAuthSePropagan() {
        when(usuarios.validar(1L)).thenThrow(new ReservasException(CodigoError.SERVICIO_NO_DISPONIBLE, "caído"));
        assertCodigo(CodigoError.SERVICIO_NO_DISPONIBLE, () -> validador.exigirAdmin(1L));
    }
}
