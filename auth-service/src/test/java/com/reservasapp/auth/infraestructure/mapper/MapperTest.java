package com.reservasapp.auth.infraestructure.mapper;

import com.reservasapp.auth.domain.model.Rol;
import com.reservasapp.auth.domain.model.Usuario;
import com.reservasapp.auth.infraestructure.driver_adapters.jpa_repository.UsuarioData;
import com.reservasapp.auth.infraestructure.entry_points.dto.ActualizarUsuarioRequest;
import com.reservasapp.auth.infraestructure.entry_points.dto.CrearUsuarioRequest;
import com.reservasapp.auth.infraestructure.entry_points.dto.LoginResponse;
import com.reservasapp.auth.infraestructure.entry_points.dto.UsuarioDTO;
import com.reservasapp.auth.infraestructure.entry_points.dto.ValidarResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapperTest {

    private Usuario usuario() {
        return Usuario.builder().id(1L).nombre("Ana").correo("ana@mail.com").clave("hash").edad(25)
                .numeroTelefonico("300").rol(Rol.ADMIN).activo(true).telegramChatId("tg").build();
    }

    @Test
    void usuarioMapper_ida_y_vuelta_conservaTodosLosCampos() {
        UsuarioData data = UsuarioMapper.toData(usuario());
        Usuario vuelta = UsuarioMapper.toDomain(data);

        assertEquals(1L, data.getId());
        assertEquals("hash", data.getClave());
        assertEquals(Rol.ADMIN, data.getRol());
        assertEquals("tg", data.getTelegramChatId());
        assertEquals("ana@mail.com", vuelta.getCorreo());
        assertEquals(25, vuelta.getEdad());
        assertEquals("300", vuelta.getNumeroTelefonico());
        assertTrue(vuelta.isActivo());
    }

    @Test
    void usuarioMapper_conNulos_devuelveNulo() {
        assertNull(UsuarioMapper.toDomain(null));
        assertNull(UsuarioMapper.toData(null));
    }

    @Test
    void dtoMapper_crearRequest_aDominio_sinIdNiRol() {
        Usuario resultado = UsuarioDtoMapper.toUsuario(
                new CrearUsuarioRequest("Ana", "ana@mail.com", "secreta1", 25, "300", "tg"));

        assertNull(resultado.getId());
        assertNull(resultado.getRol());
        assertEquals("secreta1", resultado.getClave());
        assertEquals("tg", resultado.getTelegramChatId());
    }

    @Test
    void dtoMapper_actualizarRequest_aDominio() {
        Usuario resultado = UsuarioDtoMapper.toUsuario(new ActualizarUsuarioRequest(7L, "Ana", 30, "301", "tg2"));

        assertEquals(7L, resultado.getId());
        assertEquals("Ana", resultado.getNombre());
        assertEquals(30, resultado.getEdad());
        assertEquals("301", resultado.getNumeroTelefonico());
        assertEquals("tg2", resultado.getTelegramChatId());
    }

    @Test
    void dtoMapper_toDto_noExponeClave() {
        UsuarioDTO dto = UsuarioDtoMapper.toDto(usuario());

        assertEquals(1L, dto.id());
        assertEquals("ana@mail.com", dto.correo());
        assertEquals(Rol.ADMIN, dto.rol());
        assertTrue(dto.activo());
        assertEquals(25, dto.edad());
        assertEquals("300", dto.numeroTelefonico());
        assertEquals("tg", dto.telegramChatId());
    }

    @Test
    void dtoMapper_toLogin() {
        LoginResponse login = UsuarioDtoMapper.toLogin(usuario());

        assertEquals(1L, login.id());
        assertEquals("Ana", login.nombre());
        assertEquals("ana@mail.com", login.correo());
        assertEquals(Rol.ADMIN, login.rol());
    }

    @Test
    void dtoMapper_toValidar_permiteTelegramNulo() {
        Usuario sinTelegram = usuario();
        sinTelegram.setTelegramChatId(null);

        ValidarResponse validar = UsuarioDtoMapper.toValidar(sinTelegram);

        assertEquals(1L, validar.id());
        assertEquals(Rol.ADMIN, validar.rol());
        assertTrue(validar.activo());
        assertNull(validar.telegramChatId());
    }
}
