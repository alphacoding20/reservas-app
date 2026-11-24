package com.reservasapp.canchas.infraestructure.entry_points;

import com.reservasapp.canchas.domain.exception.NegocioException;
import com.reservasapp.canchas.domain.model.Cancha;
import com.reservasapp.canchas.domain.model.EstadoCancha;
import com.reservasapp.canchas.domain.model.TipoDeporte;
import com.reservasapp.canchas.domain.usecase.AutorizacionUseCase;
import com.reservasapp.canchas.domain.usecase.CanchaUseCase;
import com.reservasapp.canchas.infraestructure.exception.GlobalExceptionHandler;
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
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

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
class CanchaControllerTest {

    private static final String CUERPO_CREAR = """
            {"nombre":"Sintética 1","tipoDeporte":"FUTBOL_5","direccion":"Calle 1","latitud":4.33,
             "longitud":-74.36,"precioPorHora":50000,"techada":false,"horaApertura":"06:00","horaCierre":"22:00"}
            """;
    private static final String CUERPO_ACTUALIZAR = """
            {"id":1,"nombre":"Sintética 1","tipoDeporte":"FUTBOL_5","direccion":"Calle 1","latitud":4.33,
             "longitud":-74.36,"precioPorHora":55000,"techada":false,"horaApertura":"06:00","horaCierre":"22:00",
             "estado":"MANTENIMIENTO"}
            """;

    @Mock
    private AutorizacionUseCase autorizacion;
    @Mock
    private CanchaUseCase canchaUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-12-01T15:15:30Z"), ZoneId.of("America/Bogota"));
        mockMvc = MockMvcBuilders.standaloneSetup(new CanchaController(autorizacion, canchaUseCase))
                .setControllerAdvice(new GlobalExceptionHandler(clock))
                .build();
    }

    private Cancha cancha(Long id, EstadoCancha estado) {
        return Cancha.builder().id(id).nombre("Sintética 1").tipoDeporte(TipoDeporte.FUTBOL_5)
                .direccion("Calle 1").latitud(4.33).longitud(-74.36).precioPorHora(50000L).techada(false)
                .horaApertura(LocalTime.of(6, 0)).horaCierre(LocalTime.of(22, 0)).estado(estado).build();
    }

    // ---------- POST /api/canchas (ADMIN) ----------

    @Test
    void crear_admin_ok_devuelve201ConHorasEnFormatoHHmm() throws Exception {
        when(canchaUseCase.crear(any(Cancha.class))).thenReturn(cancha(1L, EstadoCancha.ACTIVA));

        mockMvc.perform(post("/api/canchas")
                        .header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_CREAR))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tipoDeporte").value("FUTBOL_5"))
                .andExpect(jsonPath("$.horaApertura").value("06:00"))
                .andExpect(jsonPath("$.horaCierre").value("22:00"))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));

        InOrder orden = inOrder(autorizacion, canchaUseCase);
        orden.verify(autorizacion).exigirAdmin(1L);
        orden.verify(canchaUseCase).crear(any(Cancha.class));
    }

    @Test
    void crear_sinHeader_devuelve401() throws Exception {
        doThrow(NegocioException.sinHeader()).when(autorizacion).exigirAdmin(null);

        mockMvc.perform(post("/api/canchas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_CREAR))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("NO_AUTORIZADO"));

        verifyNoInteractions(canchaUseCase);
    }

    @Test
    void crear_noAdmin_devuelve403() throws Exception {
        doThrow(NegocioException.sinPermiso()).when(autorizacion).exigirAdmin(2L);

        mockMvc.perform(post("/api/canchas")
                        .header("X-Usuario-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_CREAR))
                .andExpect(status().isForbidden());

        verifyNoInteractions(canchaUseCase);
    }

    @Test
    void crear_authCaido_devuelve502() throws Exception {
        doThrow(NegocioException.servicioNoDisponible()).when(autorizacion).exigirAdmin(1L);

        mockMvc.perform(post("/api/canchas")
                        .header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_CREAR))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("SERVICIO_NO_DISPONIBLE"));
    }

    @Test
    void crear_datosInvalidos_devuelve400Validacion() throws Exception {
        mockMvc.perform(post("/api/canchas")
                        .header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"","tipoDeporte":"FUTBOL_5","direccion":"Calle 1","latitud":4.33,
                                 "longitud":-74.36,"precioPorHora":-5,"techada":false,
                                 "horaApertura":"6:00","horaCierre":"22:00"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"))
                .andExpect(jsonPath("$.ruta").value("/api/canchas"));

        verifyNoInteractions(canchaUseCase);
    }

    @Test
    void crear_deporteInexistente_devuelve400() throws Exception {
        mockMvc.perform(post("/api/canchas")
                        .header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_CREAR.replace("FUTBOL_5", "CRICKET")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
    }

    @Test
    void crear_horarioInvalido_devuelve400HorarioInvalido() throws Exception {
        when(canchaUseCase.crear(any(Cancha.class)))
                .thenThrow(NegocioException.horarioInvalido("Apertura y cierre inválidos"));

        mockMvc.perform(post("/api/canchas")
                        .header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_CREAR))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("HORARIO_INVALIDO"));
    }

    // ---------- GET ----------

    @Test
    void listar_devuelveArregloSimple() throws Exception {
        when(canchaUseCase.listar()).thenReturn(List.of(cancha(1L, EstadoCancha.ACTIVA), cancha(2L, EstadoCancha.INACTIVA)));

        mockMvc.perform(get("/api/canchas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].estado").value("INACTIVA"));
    }

    @Test
    void obtener_ok() throws Exception {
        when(canchaUseCase.obtener(1L)).thenReturn(cancha(1L, EstadoCancha.ACTIVA));

        mockMvc.perform(get("/api/canchas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Sintética 1"))
                .andExpect(jsonPath("$.precioPorHora").value(50000))
                .andExpect(jsonPath("$.techada").value(false));
    }

    @Test
    void obtener_noExiste_devuelve404ConFormatoDeError() throws Exception {
        when(canchaUseCase.obtener(99L)).thenThrow(NegocioException.canchaNoEncontrada());

        mockMvc.perform(get("/api/canchas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").value("2026-12-01T10:15:30"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("CANCHA_NO_ENCONTRADA"))
                .andExpect(jsonPath("$.ruta").value("/api/canchas/99"));
    }

    @Test
    void obtener_idNoNumerico_devuelve400() throws Exception {
        mockMvc.perform(get("/api/canchas/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
    }

    @Test
    void obtener_errorInesperado_devuelve500() throws Exception {
        when(canchaUseCase.obtener(5L)).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/canchas/5"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("ERROR_INTERNO"));
    }

    // ---------- PUT /{id} (ADMIN) ----------

    @Test
    void actualizar_admin_ok_ignoraElIdDelCuerpoYUsaElDeLaRuta() throws Exception {
        when(canchaUseCase.actualizar(any(Long.class), any(Cancha.class)))
                .thenReturn(cancha(1L, EstadoCancha.MANTENIMIENTO));

        mockMvc.perform(put("/api/canchas/1")
                        .header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_ACTUALIZAR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("MANTENIMIENTO"));

        verify(autorizacion).exigirAdmin(1L);
        verify(canchaUseCase).actualizar(any(Long.class), any(Cancha.class));
    }

    @Test
    void actualizar_sinEstado_devuelve400() throws Exception {
        mockMvc.perform(put("/api/canchas/1")
                        .header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_CREAR))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
    }

    @Test
    void actualizar_noAdmin_devuelve403() throws Exception {
        doThrow(NegocioException.sinPermiso()).when(autorizacion).exigirAdmin(2L);

        mockMvc.perform(put("/api/canchas/1")
                        .header("X-Usuario-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_ACTUALIZAR))
                .andExpect(status().isForbidden());
    }

    @Test
    void actualizar_noExiste_devuelve404() throws Exception {
        when(canchaUseCase.actualizar(any(Long.class), any(Cancha.class)))
                .thenThrow(NegocioException.canchaNoEncontrada());

        mockMvc.perform(put("/api/canchas/50")
                        .header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_ACTUALIZAR))
                .andExpect(status().isNotFound());
    }

    // ---------- DELETE /{id} (ADMIN) ----------

    @Test
    void eliminar_admin_ok_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/canchas/1").header("X-Usuario-Id", "1"))
                .andExpect(status().isNoContent());

        verify(autorizacion).exigirAdmin(1L);
        verify(canchaUseCase).eliminar(1L);
    }

    @Test
    void eliminar_sinHeader_devuelve401() throws Exception {
        doThrow(NegocioException.sinHeader()).when(autorizacion).exigirAdmin(null);

        mockMvc.perform(delete("/api/canchas/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(canchaUseCase);
    }

    @Test
    void eliminar_noExiste_devuelve404() throws Exception {
        doThrow(NegocioException.canchaNoEncontrada()).when(canchaUseCase).eliminar(9L);

        mockMvc.perform(delete("/api/canchas/9").header("X-Usuario-Id", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("CANCHA_NO_ENCONTRADA"));
    }

    // ---------- GET /buscar ----------

    @Test
    void buscar_conFiltros_losPasaAlCasoDeUso() throws Exception {
        when(canchaUseCase.buscar(TipoDeporte.PADEL, true, 70000L))
                .thenReturn(List.of(cancha(3L, EstadoCancha.ACTIVA)));

        mockMvc.perform(get("/api/canchas/buscar")
                        .param("deporte", "PADEL")
                        .param("techada", "true")
                        .param("precioMax", "70000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    void buscar_sinFiltros_pasaNulos() throws Exception {
        when(canchaUseCase.buscar(null, null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/canchas/buscar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void buscar_deporteInvalido_devuelve400() throws Exception {
        mockMvc.perform(get("/api/canchas/buscar").param("deporte", "CRICKET"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
    }
}
