package com.reservasapp.auth.infraestructure.entry_points;

import com.reservasapp.auth.domain.exception.NegocioException;
import com.reservasapp.auth.domain.model.Rol;
import com.reservasapp.auth.domain.model.Usuario;
import com.reservasapp.auth.domain.usecase.UsuarioUseCase;
import com.reservasapp.auth.infraestructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioUseCase useCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-12-01T15:15:30Z"), ZoneId.of("America/Bogota"));
        mockMvc = MockMvcBuilders.standaloneSetup(new UsuarioController(useCase))
                .setControllerAdvice(new GlobalExceptionHandler(clock))
                .build();
    }

    private Usuario usuario(Long id, Rol rol, boolean activo, String telegram) {
        return Usuario.builder().id(id).nombre("Ana Pérez").correo("ana@mail.com").clave("hashSecreto")
                .edad(25).numeroTelefonico("3001234567").rol(rol).activo(activo).telegramChatId(telegram).build();
    }

    // ---------- POST /save ----------

    @Test
    void guardar_ok_devuelve201SinExponerLaClave() throws Exception {
        when(useCase.registrar(any(Usuario.class))).thenReturn(usuario(1L, Rol.USUARIO, true, "123456789"));

        mockMvc.perform(post("/api/auth/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana Pérez","correo":"ana@mail.com","clave":"secreta1",
                                 "edad":25,"numeroTelefonico":"3001234567","telegramChatId":"123456789"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.correo").value("ana@mail.com"))
                .andExpect(jsonPath("$.rol").value("USUARIO"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.clave").doesNotExist());
    }

    @Test
    void guardar_datosInvalidos_devuelve400Validacion() throws Exception {
        mockMvc.perform(post("/api/auth/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"","correo":"no-es-correo","clave":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDACION"))
                .andExpect(jsonPath("$.ruta").value("/api/auth/save"));

        verifyNoInteractions(useCase);
    }

    @Test
    void guardar_jsonMalformado_devuelve400Validacion() throws Exception {
        mockMvc.perform(post("/api/auth/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
    }

    @Test
    void guardar_correoDuplicado_devuelve409() throws Exception {
        when(useCase.registrar(any(Usuario.class))).thenThrow(NegocioException.correoDuplicado());

        mockMvc.perform(post("/api/auth/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana","correo":"ana@mail.com","clave":"secreta1"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CORREO_DUPLICADO"));
    }

    // ---------- GET /{id} ----------

    @Test
    void obtener_ok() throws Exception {
        when(useCase.obtener(1L)).thenReturn(usuario(1L, Rol.USUARIO, true, null));

        mockMvc.perform(get("/api/auth/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Ana Pérez"));
    }

    @Test
    void obtener_noExiste_devuelve404ConFormatoDeError() throws Exception {
        when(useCase.obtener(99L)).thenThrow(NegocioException.usuarioNoEncontrado());

        mockMvc.perform(get("/api/auth/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").value("2026-12-01T10:15:30"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("USUARIO_NO_ENCONTRADO"))
                .andExpect(jsonPath("$.mensaje").value("El usuario no existe"))
                .andExpect(jsonPath("$.ruta").value("/api/auth/99"));
    }

    @Test
    void obtener_idNoNumerico_devuelve400() throws Exception {
        mockMvc.perform(get("/api/auth/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
    }

    @Test
    void obtener_errorInesperado_devuelve500() throws Exception {
        when(useCase.obtener(5L)).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/auth/5"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("ERROR_INTERNO"));
    }

    // ---------- PUT /update ----------

    @Test
    void actualizar_ok() throws Exception {
        when(useCase.actualizar(any(Usuario.class))).thenReturn(usuario(1L, Rol.USUARIO, true, "555"));

        mockMvc.perform(put("/api/auth/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":1,"nombre":"Ana Pérez","edad":25,"numeroTelefonico":"3001234567","telegramChatId":"555"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telegramChatId").value("555"));
    }

    @Test
    void actualizar_sinId_devuelve400() throws Exception {
        mockMvc.perform(put("/api/auth/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
    }

    @Test
    void actualizar_noExiste_devuelve404() throws Exception {
        when(useCase.actualizar(any(Usuario.class))).thenThrow(NegocioException.usuarioNoEncontrado());

        mockMvc.perform(put("/api/auth/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":50,"nombre":"Ana"}
                                """))
                .andExpect(status().isNotFound());
    }

    // ---------- DELETE /{id} ----------

    @Test
    void eliminar_ok_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/auth/1"))
                .andExpect(status().isNoContent());

        verify(useCase).eliminar(1L);
    }

    @Test
    void eliminar_noExiste_devuelve404() throws Exception {
        doThrow(NegocioException.usuarioNoEncontrado()).when(useCase).eliminar(9L);

        mockMvc.perform(delete("/api/auth/9"))
                .andExpect(status().isNotFound());
    }

    // ---------- POST /login ----------

    @Test
    void login_ok() throws Exception {
        when(useCase.login("ana@mail.com", "secreta1")).thenReturn(usuario(1L, Rol.USUARIO, true, null));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"ana@mail.com","clave":"secreta1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rol").value("USUARIO"))
                .andExpect(jsonPath("$.clave").doesNotExist());
    }

    @Test
    void login_credencialesInvalidas_devuelve401() throws Exception {
        when(useCase.login("ana@mail.com", "mala")).thenThrow(NegocioException.credencialesInvalidas());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"ana@mail.com","clave":"mala"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    void login_usuarioInactivo_devuelve403() throws Exception {
        when(useCase.login("ana@mail.com", "secreta1")).thenThrow(NegocioException.usuarioInactivo());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"ana@mail.com","clave":"secreta1"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("USUARIO_INACTIVO"));
    }

    @Test
    void login_sinCampos_devuelve400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
    }

    // ---------- PUT /{id}/clave ----------

    @Test
    void cambiarClave_ok_devuelve204() throws Exception {
        mockMvc.perform(put("/api/auth/1/clave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"claveActual":"vieja123","claveNueva":"nueva123"}
                                """))
                .andExpect(status().isNoContent());

        verify(useCase).cambiarClave(1L, "vieja123", "nueva123");
    }

    @Test
    void cambiarClave_claveActualIncorrecta_devuelve400() throws Exception {
        doThrow(NegocioException.claveIncorrecta()).when(useCase).cambiarClave(1L, "mala", "nueva123");

        mockMvc.perform(put("/api/auth/1/clave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"claveActual":"mala","claveNueva":"nueva123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("CLAVE_INCORRECTA"));
    }

    @Test
    void cambiarClave_claveNuevaCorta_devuelve400Validacion() throws Exception {
        mockMvc.perform(put("/api/auth/1/clave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"claveActual":"vieja123","claveNueva":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));

        verifyNoInteractions(useCase);
    }

    // ---------- GET /correo/{correo} ----------

    @Test
    void buscarPorCorreo_ok() throws Exception {
        when(useCase.buscarPorCorreo("ana@mail.com")).thenReturn(usuario(1L, Rol.USUARIO, true, null));

        mockMvc.perform(get("/api/auth/correo/ana@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("ana@mail.com"));
    }

    @Test
    void buscarPorCorreo_noExiste_devuelve404() throws Exception {
        when(useCase.buscarPorCorreo("x@mail.com")).thenThrow(NegocioException.usuarioNoEncontrado());

        mockMvc.perform(get("/api/auth/correo/x@mail.com"))
                .andExpect(status().isNotFound());
    }

    // ---------- PUT /{id}/rol (ADMIN) ----------

    @Test
    void cambiarRol_admin_ok_validaAdminAntesDeCambiar() throws Exception {
        when(useCase.cambiarRol(2L, Rol.ADMIN)).thenReturn(usuario(2L, Rol.ADMIN, true, null));

        mockMvc.perform(put("/api/auth/2/rol")
                        .header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rol":"ADMIN"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMIN"));

        InOrder orden = inOrder(useCase);
        orden.verify(useCase).exigirAdmin(1L);
        orden.verify(useCase).cambiarRol(2L, Rol.ADMIN);
    }

    @Test
    void cambiarRol_sinHeader_devuelve401() throws Exception {
        doThrow(NegocioException.sinHeader()).when(useCase).exigirAdmin(null);

        mockMvc.perform(put("/api/auth/2/rol")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rol":"ADMIN"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("NO_AUTORIZADO"));
    }

    @Test
    void cambiarRol_noAdmin_devuelve403() throws Exception {
        doThrow(NegocioException.sinPermiso()).when(useCase).exigirAdmin(2L);

        mockMvc.perform(put("/api/auth/2/rol")
                        .header("X-Usuario-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rol":"ADMIN"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("NO_AUTORIZADO"));
    }

    @Test
    void cambiarRol_valorInvalido_devuelve400() throws Exception {
        mockMvc.perform(put("/api/auth/2/rol")
                        .header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rol":"SUPERADMIN"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
    }

    // ---------- PUT /{id}/estado (ADMIN) ----------

    @Test
    void cambiarEstado_admin_ok() throws Exception {
        when(useCase.cambiarEstado(2L, false)).thenReturn(usuario(2L, Rol.USUARIO, false, null));

        mockMvc.perform(put("/api/auth/2/estado")
                        .header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"activo":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        verify(useCase).exigirAdmin(1L);
    }

    @Test
    void cambiarEstado_noAdmin_devuelve403() throws Exception {
        doThrow(NegocioException.sinPermiso()).when(useCase).exigirAdmin(3L);

        mockMvc.perform(put("/api/auth/2/estado")
                        .header("X-Usuario-Id", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"activo":false}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void cambiarEstado_sinCampoActivo_devuelve400() throws Exception {
        mockMvc.perform(put("/api/auth/2/estado")
                        .header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
    }

    // ---------- GET /{id}/validar ----------

    @Test
    void validar_ok_incluyeTelegramChatId() throws Exception {
        when(useCase.validar(3L)).thenReturn(usuario(3L, Rol.USUARIO, true, "123456789"));

        mockMvc.perform(get("/api/auth/3/validar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.nombre").value("Ana Pérez"))
                .andExpect(jsonPath("$.correo").value("ana@mail.com"))
                .andExpect(jsonPath("$.rol").value("USUARIO"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.telegramChatId").value("123456789"))
                .andExpect(jsonPath("$.clave").doesNotExist());
    }

    @Test
    void validar_usuarioInactivo_responde200ConActivoFalse() throws Exception {
        when(useCase.validar(2L)).thenReturn(usuario(2L, Rol.USUARIO, false, null));

        mockMvc.perform(get("/api/auth/2/validar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    void validar_sinTelegram_laClaveSiempreEstaPresenteConNull() throws Exception {
        when(useCase.validar(2L)).thenReturn(usuario(2L, Rol.USUARIO, true, null));

        mockMvc.perform(get("/api/auth/2/validar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telegramChatId").hasJsonPath())
                .andExpect(jsonPath("$.telegramChatId").isEmpty());
    }

    @Test
    void validar_noExiste_devuelve404() throws Exception {
        when(useCase.validar(99L)).thenThrow(NegocioException.usuarioNoEncontrado());

        mockMvc.perform(get("/api/auth/99/validar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("USUARIO_NO_ENCONTRADO"));
    }
}
