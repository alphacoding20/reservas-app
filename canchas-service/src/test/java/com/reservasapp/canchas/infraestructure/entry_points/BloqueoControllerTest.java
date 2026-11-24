package com.reservasapp.canchas.infraestructure.entry_points;

import com.reservasapp.canchas.domain.exception.NegocioException;
import com.reservasapp.canchas.domain.model.Bloqueo;
import com.reservasapp.canchas.domain.usecase.AutorizacionUseCase;
import com.reservasapp.canchas.domain.usecase.BloqueoUseCase;
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
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BloqueoControllerTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 12, 8, 14, 0);
    private static final LocalDateTime FIN = LocalDateTime.of(2026, 12, 8, 16, 0);
    private static final String CUERPO = """
            {"fechaInicio":"2026-12-08T14:00:00","fechaFin":"2026-12-08T16:00:00","motivo":"Mantenimiento"}
            """;

    @Mock
    private AutorizacionUseCase autorizacion;
    @Mock
    private BloqueoUseCase bloqueoUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-12-01T15:15:30Z"), ZoneId.of("America/Bogota"));
        mockMvc = MockMvcBuilders.standaloneSetup(new BloqueoController(autorizacion, bloqueoUseCase))
                .setControllerAdvice(new GlobalExceptionHandler(clock))
                .build();
    }

    @Test
    void crear_admin_ok_devuelve201() throws Exception {
        when(bloqueoUseCase.crear(1L, INICIO, FIN, "Mantenimiento")).thenReturn(
                Bloqueo.builder().id(9L).canchaId(1L).fechaInicio(INICIO).fechaFin(FIN).motivo("Mantenimiento").build());

        mockMvc.perform(post("/api/canchas/1/bloqueos")
                        .header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.canchaId").value(1))
                .andExpect(jsonPath("$.fechaInicio").value("2026-12-08T14:00:00"))
                .andExpect(jsonPath("$.fechaFin").value("2026-12-08T16:00:00"))
                .andExpect(jsonPath("$.motivo").value("Mantenimiento"));

        InOrder orden = inOrder(autorizacion, bloqueoUseCase);
        orden.verify(autorizacion).exigirAdmin(1L);
        orden.verify(bloqueoUseCase).crear(1L, INICIO, FIN, "Mantenimiento");
    }

    @Test
    void crear_sinHeader_devuelve401() throws Exception {
        doThrow(NegocioException.sinHeader()).when(autorizacion).exigirAdmin(null);

        mockMvc.perform(post("/api/canchas/1/bloqueos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(bloqueoUseCase);
    }

    @Test
    void crear_noAdmin_devuelve403() throws Exception {
        doThrow(NegocioException.sinPermiso()).when(autorizacion).exigirAdmin(2L);

        mockMvc.perform(post("/api/canchas/1/bloqueos")
                        .header("X-Usuario-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bloqueoUseCase);
    }

    @Test
    void crear_sinMotivo_devuelve400Validacion() throws Exception {
        mockMvc.perform(post("/api/canchas/1/bloqueos")
                        .header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fechaInicio":"2026-12-08T14:00:00","fechaFin":"2026-12-08T16:00:00"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));

        verifyNoInteractions(bloqueoUseCase);
    }

    @Test
    void crear_fechaMalFormada_devuelve400() throws Exception {
        mockMvc.perform(post("/api/canchas/1/bloqueos")
                        .header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fechaInicio":"ayer","fechaFin":"2026-12-08T16:00:00","motivo":"x"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
    }

    @Test
    void crear_canchaNoExiste_devuelve404() throws Exception {
        when(bloqueoUseCase.crear(7L, INICIO, FIN, "Mantenimiento"))
                .thenThrow(NegocioException.canchaNoEncontrada());

        mockMvc.perform(post("/api/canchas/7/bloqueos")
                        .header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("CANCHA_NO_ENCONTRADA"));
    }

    @Test
    void crear_horarioInvalido_devuelve400() throws Exception {
        when(bloqueoUseCase.crear(1L, INICIO, FIN, "Mantenimiento"))
                .thenThrow(NegocioException.horarioInvalido("Bloqueo inválido"));

        mockMvc.perform(post("/api/canchas/1/bloqueos")
                        .header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("HORARIO_INVALIDO"));
    }

    @Test
    void eliminar_admin_ok_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/canchas/1/bloqueos/9").header("X-Usuario-Id", "1"))
                .andExpect(status().isNoContent());

        verify(autorizacion).exigirAdmin(1L);
        verify(bloqueoUseCase).eliminar(1L, 9L);
    }

    @Test
    void eliminar_sinHeader_devuelve401() throws Exception {
        doThrow(NegocioException.sinHeader()).when(autorizacion).exigirAdmin(null);

        mockMvc.perform(delete("/api/canchas/1/bloqueos/9"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(bloqueoUseCase);
    }

    @Test
    void eliminar_bloqueoNoExiste_devuelve404() throws Exception {
        doThrow(NegocioException.bloqueoNoEncontrado()).when(bloqueoUseCase).eliminar(1L, 99L);

        mockMvc.perform(delete("/api/canchas/1/bloqueos/99").header("X-Usuario-Id", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("BLOQUEO_NO_ENCONTRADO"));
    }
}
