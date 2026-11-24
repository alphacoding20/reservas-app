package com.reservasapp.notificaciones.infraestructure.entry_points;

import com.reservasapp.notificaciones.TestData;
import com.reservasapp.notificaciones.domain.model.ConteoEnvios;
import com.reservasapp.notificaciones.domain.model.EstadisticasNotificaciones;
import com.reservasapp.notificaciones.domain.model.EstadoNotificacion;
import com.reservasapp.notificaciones.domain.model.NotificacionNoEncontradaException;
import com.reservasapp.notificaciones.domain.model.SolicitudNotificacion;
import com.reservasapp.notificaciones.domain.model.TipoNotificacion;
import com.reservasapp.notificaciones.domain.model.ValidacionException;
import com.reservasapp.notificaciones.domain.model.YaEnviadaException;
import com.reservasapp.notificaciones.domain.usecase.ConsultarNotificacionesUseCase;
import com.reservasapp.notificaciones.domain.usecase.EnviarNotificacionUseCase;
import com.reservasapp.notificaciones.domain.usecase.EstadisticasNotificacionesUseCase;
import com.reservasapp.notificaciones.domain.usecase.ReenviarNotificacionUseCase;
import com.reservasapp.notificaciones.infraestructure.mapper.NotificacionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc en modo standalone: prueba rutas, codigos HTTP y el JSON de error del contrato (2.4)
 * sin levantar Spring ni la base de datos.
 */
@ExtendWith(MockitoExtension.class)
class NotificacionControllerTest {

    private static final String CUERPO_CONFIRMACION = """
            {
              "usuarioId": 3,
              "nombreUsuario": "Ana Pérez",
              "canal": "TELEGRAM",
              "destinatario": "123456789",
              "reservaId": 55,
              "nombreCancha": "Sintética 1",
              "fecha": "2026-12-08",
              "horaInicio": "18:00",
              "horaFin": "19:00",
              "precioTotal": 60000,
              "campoQueNoExiste": "se ignora"
            }""";

    @Mock
    private EnviarNotificacionUseCase enviarUseCase;
    @Mock
    private ReenviarNotificacionUseCase reenviarUseCase;
    @Mock
    private ConsultarNotificacionesUseCase consultarUseCase;
    @Mock
    private EstadisticasNotificacionesUseCase estadisticasUseCase;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        NotificacionController controller = new NotificacionController(
                enviarUseCase, reenviarUseCase, consultarUseCase, estadisticasUseCase, new NotificacionMapper());
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(TestData.CLOCK))
                .build();
    }

    // ---------- 5 endpoints de envio ----------

    @ParameterizedTest
    @CsvSource({
            "/api/notificaciones/confirmacion,CONFIRMACION",
            "/api/notificaciones/recordatorio,RECORDATORIO",
            "/api/notificaciones/cancelacion,CANCELACION",
            "/api/notificaciones/reprogramacion,REPROGRAMACION",
            "/api/notificaciones/alerta-lluvia,ALERTA_LLUVIA"})
    void cadaEndpointDeEnvioResponde201ConElTipoCorrecto(String ruta, TipoNotificacion tipo) throws Exception {
        when(enviarUseCase.ejecutar(eq(tipo), any(SolicitudNotificacion.class)))
                .thenReturn(TestData.notificacion(1L, EstadoNotificacion.ENVIADA));

        mvc.perform(post(ruta).contentType(MediaType.APPLICATION_JSON).content(CUERPO_CONFIRMACION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.usuarioId").value(3))
                .andExpect(jsonPath("$.canal").value("TELEGRAM"))
                .andExpect(jsonPath("$.estado").value("ENVIADA"))
                .andExpect(jsonPath("$.intentos").value(1));
    }

    @Test
    void elCuerpoSeConvierteASolicitudDeDominioIgnorandoCamposDesconocidos() throws Exception {
        when(enviarUseCase.ejecutar(any(), any(SolicitudNotificacion.class)))
                .thenReturn(TestData.notificacion(1L, EstadoNotificacion.ENVIADA));

        mvc.perform(post("/api/notificaciones/confirmacion")
                .contentType(MediaType.APPLICATION_JSON).content(CUERPO_CONFIRMACION))
                .andExpect(status().isCreated());

        ArgumentCaptor<SolicitudNotificacion> captor = ArgumentCaptor.forClass(SolicitudNotificacion.class);
        verify(enviarUseCase).ejecutar(eq(TipoNotificacion.CONFIRMACION), captor.capture());
        SolicitudNotificacion s = captor.getValue();
        assertThat(s.usuarioId()).isEqualTo(3L);
        assertThat(s.nombreCancha()).isEqualTo("Sintética 1");
        assertThat(s.horaInicio()).hasToString("18:00");
        assertThat(s.precioTotal()).isEqualTo(60000L);
    }

    @Test
    void unEnvioFallidoSigueRespondiendo201() throws Exception {
        when(enviarUseCase.ejecutar(any(), any(SolicitudNotificacion.class)))
                .thenReturn(TestData.notificacion(2L, EstadoNotificacion.FALLIDA));

        mvc.perform(post("/api/notificaciones/recordatorio")
                .contentType(MediaType.APPLICATION_JSON).content(CUERPO_CONFIRMACION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("FALLIDA"));
    }

    @Test
    void validacionDelDominioSeTraduceA400ConElFormatoDeError() throws Exception {
        when(enviarUseCase.ejecutar(any(), any(SolicitudNotificacion.class)))
                .thenThrow(new ValidacionException("Campos obligatorios faltantes: precioTotal"));

        mvc.perform(post("/api/notificaciones/confirmacion")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDACION"))
                .andExpect(jsonPath("$.mensaje").value("Campos obligatorios faltantes: precioTotal"))
                .andExpect(jsonPath("$.ruta").value("/api/notificaciones/confirmacion"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void jsonMalformadoOEnumInvalidoDa400Validacion() throws Exception {
        mvc.perform(post("/api/notificaciones/cancelacion")
                .contentType(MediaType.APPLICATION_JSON).content("{ esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));

        mvc.perform(post("/api/notificaciones/cancelacion")
                .contentType(MediaType.APPLICATION_JSON).content("{\"canal\":\"PALOMA_MENSAJERA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
    }

    @Test
    void sinCuerpoDa400() throws Exception {
        mvc.perform(post("/api/notificaciones/confirmacion").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
    }

    // ---------- reenviar ----------

    @Test
    void reenviarDevuelve200ConLaNotificacionActualizada() throws Exception {
        var reenviada = TestData.notificacion(9L, EstadoNotificacion.ENVIADA);
        reenviada.setIntentos(2);
        when(reenviarUseCase.ejecutar(9L)).thenReturn(reenviada);

        mvc.perform(post("/api/notificaciones/9/reenviar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENVIADA"))
                .andExpect(jsonPath("$.intentos").value(2));
    }

    @Test
    void reenviarUnaYaEnviadaDa409YaEnviada() throws Exception {
        when(reenviarUseCase.ejecutar(9L)).thenThrow(new YaEnviadaException(9L));

        mvc.perform(post("/api/notificaciones/9/reenviar"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("YA_ENVIADA"));
    }

    @Test
    void reenviarInexistenteDa404() throws Exception {
        when(reenviarUseCase.ejecutar(404L)).thenThrow(new NotificacionNoEncontradaException(404L));

        mvc.perform(post("/api/notificaciones/404/reenviar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOTIFICACION_NO_ENCONTRADA"));
    }

    // ---------- consultas ----------

    @Test
    void historialDelUsuarioEsUnArregloSimple() throws Exception {
        when(consultarUseCase.porUsuario(3L)).thenReturn(List.of(
                TestData.notificacion(2L, EstadoNotificacion.ENVIADA),
                TestData.notificacion(1L, EstadoNotificacion.FALLIDA)));

        mvc.perform(get("/api/notificaciones/usuario/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].estado").value("FALLIDA"));
    }

    @Test
    void historialSinNotificacionesEsArregloVacio() throws Exception {
        when(consultarUseCase.porUsuario(99L)).thenReturn(List.of());

        mvc.perform(get("/api/notificaciones/usuario/99"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void estadisticasIncluyenTotalesYDesglosePorTipo() throws Exception {
        Map<TipoNotificacion, ConteoEnvios> porTipo = new EnumMap<>(TipoNotificacion.class);
        porTipo.put(TipoNotificacion.CONFIRMACION, new ConteoEnvios(5, 1));
        when(estadisticasUseCase.ejecutar()).thenReturn(new EstadisticasNotificaciones(6, 5, 1, porTipo));

        mvc.perform(get("/api/notificaciones/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(6))
                .andExpect(jsonPath("$.enviadas").value(5))
                .andExpect(jsonPath("$.fallidas").value(1))
                .andExpect(jsonPath("$.porTipo.CONFIRMACION.enviadas").value(5))
                .andExpect(jsonPath("$.porTipo.CONFIRMACION.fallidas").value(1));
    }

    @Test
    void consultarPorIdDevuelve200() throws Exception {
        when(consultarUseCase.porId(1L)).thenReturn(TestData.notificacion(1L, EstadoNotificacion.ENVIADA));

        mvc.perform(get("/api/notificaciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tipo").value("CONFIRMACION"));
    }

    @Test
    void consultarInexistenteDa404ConElCodigoDelContrato() throws Exception {
        when(consultarUseCase.porId(404L)).thenThrow(new NotificacionNoEncontradaException(404L));

        mvc.perform(get("/api/notificaciones/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOTIFICACION_NO_ENCONTRADA"))
                .andExpect(jsonPath("$.ruta").value("/api/notificaciones/404"));
    }

    @Test
    void idNoNumericoDa400Validacion() throws Exception {
        mvc.perform(get("/api/notificaciones/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
    }

    @Test
    void eliminarDevuelve204() throws Exception {
        mvc.perform(delete("/api/notificaciones/1")).andExpect(status().isNoContent());

        verify(consultarUseCase).eliminar(1L);
    }

    @Test
    void eliminarInexistenteDa404() throws Exception {
        org.mockito.Mockito.doThrow(new NotificacionNoEncontradaException(5L)).when(consultarUseCase).eliminar(5L);

        mvc.perform(delete("/api/notificaciones/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOTIFICACION_NO_ENCONTRADA"));
    }

    // ---------- errores genericos ----------

    @Test
    void metodoNoPermitidoUsaElMismoFormatoDeError() throws Exception {
        mvc.perform(put("/api/notificaciones/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("METODO_NO_PERMITIDO"));
    }

    @Test
    void contentTypeQueNoEsJsonDa415() throws Exception {
        mvc.perform(post("/api/notificaciones/confirmacion").contentType(MediaType.TEXT_PLAIN).content("hola"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error").value("MEDIA_TYPE_NO_SOPORTADO"));
    }

    @Test
    void errorInesperadoDa500ErrorInternoSinFiltrarElDetalle() throws Exception {
        when(consultarUseCase.porId(1L)).thenThrow(new IllegalStateException("fallo con datos internos"));

        mvc.perform(get("/api/notificaciones/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("ERROR_INTERNO"))
                .andExpect(jsonPath("$.mensaje").value("Ocurrió un error interno"));
    }
}
