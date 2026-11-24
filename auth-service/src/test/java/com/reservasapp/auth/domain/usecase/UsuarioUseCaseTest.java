package com.reservasapp.auth.domain.usecase;

import com.reservasapp.auth.domain.exception.NegocioException;
import com.reservasapp.auth.domain.model.Rol;
import com.reservasapp.auth.domain.model.Usuario;
import com.reservasapp.auth.domain.model.gateway.ClaveGateway;
import com.reservasapp.auth.domain.model.gateway.UsuarioGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioUseCaseTest {

    @Mock
    private UsuarioGateway usuarioGateway;
    @Mock
    private ClaveGateway claveGateway;
    @InjectMocks
    private UsuarioUseCase useCase;

    private Usuario usuario(Long id, Rol rol, boolean activo) {
        return Usuario.builder()
                .id(id)
                .nombre("Ana Pérez")
                .correo("ana@mail.com")
                .clave("hashGuardado")
                .edad(25)
                .numeroTelefonico("3001234567")
                .rol(rol)
                .activo(activo)
                .telegramChatId("123456789")
                .build();
    }

    private void verificarError(NegocioException ex, int status, String codigo) {
        assertEquals(status, ex.getStatus());
        assertEquals(codigo, ex.getCodigo());
    }

    // ---------- registrar ----------

    @Test
    void registrar_ok_normalizaCorreoCodificaClaveYFuerzaRolUsuario() {
        Usuario entrada = usuario(99L, Rol.ADMIN, false);
        entrada.setCorreo("  ANA@Mail.com ");
        entrada.setClave("secreta1");
        when(usuarioGateway.existePorCorreo("ana@mail.com")).thenReturn(false);
        when(claveGateway.codificar("secreta1")).thenReturn("hash");
        when(usuarioGateway.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = useCase.registrar(entrada);

        assertNull(resultado.getId());
        assertEquals("ana@mail.com", resultado.getCorreo());
        assertEquals("hash", resultado.getClave());
        assertEquals(Rol.USUARIO, resultado.getRol());
        assertTrue(resultado.isActivo());
    }

    @Test
    void registrar_correoDuplicado_lanzaConflictoYNoGuarda() {
        when(usuarioGateway.existePorCorreo("ana@mail.com")).thenReturn(true);

        NegocioException ex = assertThrows(NegocioException.class,
                () -> useCase.registrar(usuario(null, Rol.USUARIO, true)));

        verificarError(ex, 409, "CORREO_DUPLICADO");
        verify(usuarioGateway, never()).guardar(any());
        verifyNoInteractions(claveGateway);
    }

    // ---------- obtener / validar / buscarPorCorreo ----------

    @Test
    void obtener_ok() {
        Usuario existente = usuario(1L, Rol.USUARIO, true);
        when(usuarioGateway.buscarPorId(1L)).thenReturn(Optional.of(existente));

        assertSame(existente, useCase.obtener(1L));
    }

    @Test
    void obtener_noExiste_lanza404() {
        when(usuarioGateway.buscarPorId(1L)).thenReturn(Optional.empty());

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.obtener(1L));

        verificarError(ex, 404, "USUARIO_NO_ENCONTRADO");
    }

    @Test
    void validar_ok_devuelveUsuarioAunqueEsteInactivo() {
        Usuario inactivo = usuario(2L, Rol.USUARIO, false);
        when(usuarioGateway.buscarPorId(2L)).thenReturn(Optional.of(inactivo));

        assertSame(inactivo, useCase.validar(2L));
    }

    @Test
    void validar_noExiste_lanza404() {
        when(usuarioGateway.buscarPorId(2L)).thenReturn(Optional.empty());

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.validar(2L));

        verificarError(ex, 404, "USUARIO_NO_ENCONTRADO");
    }

    @Test
    void buscarPorCorreo_ok_normalizaElCorreo() {
        Usuario existente = usuario(1L, Rol.USUARIO, true);
        when(usuarioGateway.buscarPorCorreo("ana@mail.com")).thenReturn(Optional.of(existente));

        assertSame(existente, useCase.buscarPorCorreo(" ANA@mail.com "));
    }

    @Test
    void buscarPorCorreo_noExiste_lanza404() {
        when(usuarioGateway.buscarPorCorreo("ana@mail.com")).thenReturn(Optional.empty());

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.buscarPorCorreo("ana@mail.com"));

        verificarError(ex, 404, "USUARIO_NO_ENCONTRADO");
    }

    // ---------- actualizar / eliminar ----------

    @Test
    void actualizar_ok_cambiaSoloLosCamposPermitidos() {
        Usuario existente = usuario(1L, Rol.USUARIO, true);
        when(usuarioGateway.buscarPorId(1L)).thenReturn(Optional.of(existente));
        when(usuarioGateway.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        Usuario datos = Usuario.builder().id(1L).nombre("Ana María").edad(30)
                .numeroTelefonico("3109999999").telegramChatId("987").correo("otro@mail.com").build();

        Usuario resultado = useCase.actualizar(datos);

        assertEquals("Ana María", resultado.getNombre());
        assertEquals(30, resultado.getEdad());
        assertEquals("3109999999", resultado.getNumeroTelefonico());
        assertEquals("987", resultado.getTelegramChatId());
        assertEquals("ana@mail.com", resultado.getCorreo());
    }

    @Test
    void actualizar_noExiste_lanza404() {
        when(usuarioGateway.buscarPorId(1L)).thenReturn(Optional.empty());

        NegocioException ex = assertThrows(NegocioException.class,
                () -> useCase.actualizar(Usuario.builder().id(1L).build()));

        verificarError(ex, 404, "USUARIO_NO_ENCONTRADO");
        verify(usuarioGateway, never()).guardar(any());
    }

    @Test
    void eliminar_ok() {
        when(usuarioGateway.existePorId(1L)).thenReturn(true);

        useCase.eliminar(1L);

        verify(usuarioGateway).eliminar(1L);
    }

    @Test
    void eliminar_noExiste_lanza404() {
        when(usuarioGateway.existePorId(1L)).thenReturn(false);

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.eliminar(1L));

        verificarError(ex, 404, "USUARIO_NO_ENCONTRADO");
        verify(usuarioGateway, never()).eliminar(any());
    }

    // ---------- login ----------

    @Test
    void login_ok() {
        Usuario existente = usuario(1L, Rol.USUARIO, true);
        when(usuarioGateway.buscarPorCorreo("ana@mail.com")).thenReturn(Optional.of(existente));
        when(claveGateway.coincide("secreta1", "hashGuardado")).thenReturn(true);

        assertSame(existente, useCase.login("Ana@Mail.com", "secreta1"));
    }

    @Test
    void login_correoNoExiste_lanza401() {
        when(usuarioGateway.buscarPorCorreo("ana@mail.com")).thenReturn(Optional.empty());

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.login("ana@mail.com", "x"));

        verificarError(ex, 401, "CREDENCIALES_INVALIDAS");
        verifyNoInteractions(claveGateway);
    }

    @Test
    void login_claveIncorrecta_lanza401() {
        when(usuarioGateway.buscarPorCorreo("ana@mail.com"))
                .thenReturn(Optional.of(usuario(1L, Rol.USUARIO, true)));
        when(claveGateway.coincide("mala", "hashGuardado")).thenReturn(false);

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.login("ana@mail.com", "mala"));

        verificarError(ex, 401, "CREDENCIALES_INVALIDAS");
    }

    @Test
    void login_usuarioInactivo_lanza403() {
        when(usuarioGateway.buscarPorCorreo("ana@mail.com"))
                .thenReturn(Optional.of(usuario(1L, Rol.USUARIO, false)));
        when(claveGateway.coincide("secreta1", "hashGuardado")).thenReturn(true);

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.login("ana@mail.com", "secreta1"));

        verificarError(ex, 403, "USUARIO_INACTIVO");
    }

    // ---------- cambiarClave ----------

    @Test
    void cambiarClave_ok_guardaLaClaveNuevaCodificada() {
        when(usuarioGateway.buscarPorId(1L)).thenReturn(Optional.of(usuario(1L, Rol.USUARIO, true)));
        when(claveGateway.coincide("vieja", "hashGuardado")).thenReturn(true);
        when(claveGateway.codificar("nueva123")).thenReturn("hashNuevo");

        useCase.cambiarClave(1L, "vieja", "nueva123");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioGateway).guardar(captor.capture());
        assertEquals("hashNuevo", captor.getValue().getClave());
    }

    @Test
    void cambiarClave_claveActualIncorrecta_lanza400() {
        when(usuarioGateway.buscarPorId(1L)).thenReturn(Optional.of(usuario(1L, Rol.USUARIO, true)));
        when(claveGateway.coincide("mala", "hashGuardado")).thenReturn(false);

        NegocioException ex = assertThrows(NegocioException.class,
                () -> useCase.cambiarClave(1L, "mala", "nueva123"));

        verificarError(ex, 400, "CLAVE_INCORRECTA");
        verify(usuarioGateway, never()).guardar(any());
    }

    @Test
    void cambiarClave_usuarioNoExiste_lanza404() {
        when(usuarioGateway.buscarPorId(1L)).thenReturn(Optional.empty());

        NegocioException ex = assertThrows(NegocioException.class,
                () -> useCase.cambiarClave(1L, "vieja", "nueva123"));

        verificarError(ex, 404, "USUARIO_NO_ENCONTRADO");
    }

    // ---------- cambiarRol / cambiarEstado ----------

    @Test
    void cambiarRol_ok() {
        when(usuarioGateway.buscarPorId(2L)).thenReturn(Optional.of(usuario(2L, Rol.USUARIO, true)));
        when(usuarioGateway.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(Rol.ADMIN, useCase.cambiarRol(2L, Rol.ADMIN).getRol());
    }

    @Test
    void cambiarRol_noExiste_lanza404() {
        when(usuarioGateway.buscarPorId(2L)).thenReturn(Optional.empty());

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.cambiarRol(2L, Rol.ADMIN));

        verificarError(ex, 404, "USUARIO_NO_ENCONTRADO");
    }

    @Test
    void cambiarEstado_ok() {
        when(usuarioGateway.buscarPorId(2L)).thenReturn(Optional.of(usuario(2L, Rol.USUARIO, true)));
        when(usuarioGateway.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        assertFalse(useCase.cambiarEstado(2L, false).isActivo());
    }

    @Test
    void cambiarEstado_noExiste_lanza404() {
        when(usuarioGateway.buscarPorId(2L)).thenReturn(Optional.empty());

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.cambiarEstado(2L, false));

        verificarError(ex, 404, "USUARIO_NO_ENCONTRADO");
    }

    // ---------- exigirAdmin ----------

    @Test
    void exigirAdmin_sinHeader_lanza401() {
        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.exigirAdmin(null));

        verificarError(ex, 401, "NO_AUTORIZADO");
        verifyNoInteractions(usuarioGateway);
    }

    @Test
    void exigirAdmin_usuarioNoExiste_lanza403() {
        when(usuarioGateway.buscarPorId(9L)).thenReturn(Optional.empty());

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.exigirAdmin(9L));

        verificarError(ex, 403, "NO_AUTORIZADO");
    }

    @Test
    void exigirAdmin_rolUsuario_lanza403() {
        when(usuarioGateway.buscarPorId(2L)).thenReturn(Optional.of(usuario(2L, Rol.USUARIO, true)));

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.exigirAdmin(2L));

        verificarError(ex, 403, "NO_AUTORIZADO");
    }

    @Test
    void exigirAdmin_adminInactivo_lanza403() {
        when(usuarioGateway.buscarPorId(1L)).thenReturn(Optional.of(usuario(1L, Rol.ADMIN, false)));

        NegocioException ex = assertThrows(NegocioException.class, () -> useCase.exigirAdmin(1L));

        verificarError(ex, 403, "NO_AUTORIZADO");
    }

    @Test
    void exigirAdmin_adminActivo_noLanzaNada() {
        when(usuarioGateway.buscarPorId(1L)).thenReturn(Optional.of(usuario(1L, Rol.ADMIN, true)));

        assertDoesNotThrow(() -> useCase.exigirAdmin(1L));
    }
}
