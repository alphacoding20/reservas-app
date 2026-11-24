package com.canchas.reservas.infraestructure.entry_points;

import com.canchas.reservas.domain.exception.CodigoError;
import com.canchas.reservas.domain.exception.ReservasException;
import com.canchas.reservas.domain.model.Disponibilidad;
import com.canchas.reservas.domain.model.Disponibilidad.FranjaDisponible;
import com.canchas.reservas.domain.model.Estadisticas;
import com.canchas.reservas.domain.model.Estadisticas.OcupacionCancha;
import com.canchas.reservas.domain.model.EstadoReserva;
import com.canchas.reservas.domain.model.MotivoCancelacion;
import com.canchas.reservas.domain.model.Pronostico;
import com.canchas.reservas.domain.model.Reserva;
import com.canchas.reservas.domain.model.ResumenAlertaLluvia;
import com.canchas.reservas.domain.model.ResumenRecordatorios;
import com.canchas.reservas.domain.usecase.DisponibilidadUseCase;
import com.canchas.reservas.domain.usecase.EstadisticasUseCase;
import com.canchas.reservas.domain.usecase.PronosticoUseCase;
import com.canchas.reservas.domain.usecase.RecordatorioUseCase;
import com.canchas.reservas.domain.usecase.ReservaUseCase;
import com.canchas.reservas.infraestructure.mapper.ReservaDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.canchas.reservas.Fixtures.CLOCK;
import static com.canchas.reservas.Fixtures.FUTURO;
import static com.canchas.reservas.Fixtures.confirmada;
import static com.canchas.reservas.Fixtures.h;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReservaControllerTest {

    @Mock
    ReservaUseCase reservaUseCase;
    @Mock
    DisponibilidadUseCase disponibilidadUseCase;
    @Mock
    PronosticoUseCase pronosticoUseCase;
    @Mock
    RecordatorioUseCase recordatorioUseCase;
    @Mock
    EstadisticasUseCase estadisticasUseCase;

    MockMvc mvc;

    @BeforeEach
    void preparar() {
        ReservaController controller = new ReservaController(reservaUseCase, disponibilidadUseCase,
                pronosticoUseCase, recordatorioUseCase, estadisticasUseCase, new ReservaDtoMapper());
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(CLOCK))
                .build();
    }

    private static final String CUERPO_CREAR =
            "{\"canchaId\":1,\"usuarioId\":2,\"fecha\":\"2026-12-01\",\"horaInicio\":\"18:00\",\"horaFin\":\"20:00\"}";

    // ------------------------------------------------------------------ CRUD

    @Test
    void postCreaYDevuelve201ConElFormatoDelContrato() throws Exception {
        when(reservaUseCase.crear(1L, 2L, FUTURO, h(18), h(20))).thenReturn(confirmada(7L));

        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON).content(CUERPO_CREAR))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.canchaId").value(1))
                .andExpect(jsonPath("$.usuarioId").value(2))
                .andExpect(jsonPath("$.fecha").value("2026-12-01"))
                .andExpect(jsonPath("$.horaInicio").value("18:00"))
                .andExpect(jsonPath("$.horaFin").value("20:00"))
                .andExpect(jsonPath("$.precioTotal").value(100000))
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"))
                .andExpect(jsonPath("$.fechaCreacion").value("2026-11-23T10:00:00"));
    }

    @Test
    void postConCamposFaltantesDevuelve400Validacion() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON).content("{\"canchaId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDACION"))
                .andExpect(jsonPath("$.ruta").value("/api/reservas"))
                .andExpect(jsonPath("$.timestamp").value("2026-11-23T10:00:00"));
    }

    @Test
    void postConJsonOHoraInvalidosDevuelve400Validacion() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON).content("{no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));

        String horaMala = "{\"canchaId\":1,\"usuarioId\":2,\"fecha\":\"2026-12-01\",\"horaInicio\":\"25:99\",\"horaFin\":\"20:00\"}";
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON).content(horaMala))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
    }

    @Test
    void erroresDeDominioSeTraducenConSuStatusYCodigo() throws Exception {
        when(reservaUseCase.crear(any(), any(), any(), any(), any()))
                .thenThrow(new ReservasException(CodigoError.FRANJA_OCUPADA, "La franja ya está reservada"));

        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON).content(CUERPO_CREAR))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("FRANJA_OCUPADA"))
                .andExpect(jsonPath("$.mensaje").value("La franja ya está reservada"));
    }

    @Test
    void getPorIdListarYEliminar() throws Exception {
        when(reservaUseCase.obtener(7L)).thenReturn(confirmada(7L));
        when(reservaUseCase.obtener(9L))
                .thenThrow(new ReservasException(CodigoError.RESERVA_NO_ENCONTRADA, "no existe"));
        when(reservaUseCase.listarTodas()).thenReturn(List.of(confirmada(1L), confirmada(2L)));

        mvc.perform(get("/api/reservas/7")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(7));
        mvc.perform(get("/api/reservas/9")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESERVA_NO_ENCONTRADA"));
        mvc.perform(get("/api/reservas")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(delete("/api/reservas/7")).andExpect(status().isNoContent());
        verify(reservaUseCase).eliminar(7L);
    }

    @Test
    void idNoNumericoDevuelve400() throws Exception {
        mvc.perform(get("/api/reservas/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
    }

    // -------------------------------------------------------------- no CRUD

    @Test
    void disponibilidadDevuelveLasFranjas() throws Exception {
        when(disponibilidadUseCase.consultar(1L, FUTURO)).thenReturn(new Disponibilidad(1L, FUTURO,
                List.of(new FranjaDisponible(h(6), h(7), true), new FranjaDisponible(h(7), h(8), false))));

        mvc.perform(get("/api/reservas/disponibilidad").param("canchaId", "1").param("fecha", "2026-12-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canchaId").value(1))
                .andExpect(jsonPath("$.fecha").value("2026-12-01"))
                .andExpect(jsonPath("$.franjas[0].horaInicio").value("06:00"))
                .andExpect(jsonPath("$.franjas[0].disponible").value(true))
                .andExpect(jsonPath("$.franjas[1].disponible").value(false));
    }

    @Test
    void disponibilidadValidaParametros() throws Exception {
        mvc.perform(get("/api/reservas/disponibilidad").param("canchaId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
        mvc.perform(get("/api/reservas/disponibilidad").param("canchaId", "1").param("fecha", "mañana"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
    }

    @Test
    void cancelarUsaUsuarioPorDefectoYAceptaOtroMotivo() throws Exception {
        when(reservaUseCase.cancelar(eq(1L), any())).thenReturn(confirmada(1L).cancelada(MotivoCancelacion.USUARIO));

        mvc.perform(patch("/api/reservas/1/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"))
                .andExpect(jsonPath("$.motivoCancelacion").value("USUARIO"));
        verify(reservaUseCase).cancelar(1L, MotivoCancelacion.USUARIO);

        mvc.perform(patch("/api/reservas/1/cancelar").param("motivo", "LLUVIA")).andExpect(status().isOk());
        verify(reservaUseCase).cancelar(1L, MotivoCancelacion.LLUVIA);

        mvc.perform(patch("/api/reservas/1/cancelar").param("motivo", "XX"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
    }

    @Test
    void reprogramarDevuelveLaReservaActualizada() throws Exception {
        when(reservaUseCase.reprogramar(1L, FUTURO, h(10), h(11))).thenReturn(confirmada(1L));

        mvc.perform(patch("/api/reservas/1/reprogramar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha\":\"2026-12-01\",\"horaInicio\":\"10:00\",\"horaFin\":\"11:00\"}"))
                .andExpect(status().isOk());
        verify(reservaUseCase).reprogramar(1L, FUTURO, h(10), h(11));

        mvc.perform(patch("/api/reservas/1/reprogramar").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pronosticoDevuelveElFormatoDelContrato() throws Exception {
        when(pronosticoUseCase.pronosticoDeReserva(1L)).thenReturn(new Pronostico(1L, true, true, 80, true, 60));

        mvc.perform(get("/api/reservas/1/pronostico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservaId").value(1))
                .andExpect(jsonPath("$.aplica").value(true))
                .andExpect(jsonPath("$.pronosticoDisponible").value(true))
                .andExpect(jsonPath("$.probabilidadLluvia").value(80))
                .andExpect(jsonPath("$.riesgoLluvia").value(true))
                .andExpect(jsonPath("$.umbral").value(60));
    }

    @Test
    void alertaDeLluviaPasaElHeaderDeAdmin() throws Exception {
        when(pronosticoUseCase.alertarLluvia(1L, FUTURO)).thenReturn(new ResumenAlertaLluvia(FUTURO, 5, 2, 1));

        mvc.perform(post("/api/reservas/alertas/lluvia").header("X-Usuario-Id", "1").param("fecha", "2026-12-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value("2026-12-01"))
                .andExpect(jsonPath("$.reservasRevisadas").value(5))
                .andExpect(jsonPath("$.alertasEnviadas").value(2))
                .andExpect(jsonPath("$.sinPronostico").value(1));
    }

    @Test
    void endpointsAdminSinHeaderDevuelven401YSinPermisoDevuelven403() throws Exception {
        when(pronosticoUseCase.alertarLluvia(isNull(), any()))
                .thenThrow(new ReservasException(CodigoError.SIN_CREDENCIALES, "Falta el header"));
        when(recordatorioUseCase.enviarManual(eq(2L), any()))
                .thenThrow(new ReservasException(CodigoError.PERMISO_DENEGADO, "No es admin"));

        mvc.perform(post("/api/reservas/alertas/lluvia").param("fecha", "2026-12-01"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("NO_AUTORIZADO"));
        mvc.perform(post("/api/reservas/recordatorios").header("X-Usuario-Id", "2").param("fecha", "2026-12-01"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("NO_AUTORIZADO"));
    }

    @Test
    void recordatoriosDevuelveElResumen() throws Exception {
        when(recordatorioUseCase.enviarManual(1L, FUTURO)).thenReturn(new ResumenRecordatorios(FUTURO, 3));

        mvc.perform(post("/api/reservas/recordatorios").header("X-Usuario-Id", "1").param("fecha", "2026-12-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordatoriosEnviados").value(3));
    }

    @Test
    void asistenciaRecibeElEstadoEnElCuerpo() throws Exception {
        when(reservaUseCase.registrarAsistencia(1L, 4L, EstadoReserva.COMPLETADA))
                .thenReturn(confirmada(4L).conEstado(EstadoReserva.COMPLETADA));

        mvc.perform(patch("/api/reservas/4/asistencia").header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"COMPLETADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"));

        mvc.perform(patch("/api/reservas/4/asistencia").header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"INVENTADO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
    }

    @Test
    void historialYAgenda() throws Exception {
        Reserva r = confirmada(1L);
        when(reservaUseCase.historialDeUsuario(2L)).thenReturn(List.of(r));
        when(reservaUseCase.agendaDeCancha(1L, FUTURO)).thenReturn(List.of(r, r));

        mvc.perform(get("/api/reservas/usuario/2")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/reservas/cancha/1/agenda").param("fecha", "2026-12-01")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void estadisticasDevuelveElFormatoDelContrato() throws Exception {
        Map<EstadoReserva, Long> porEstado = new EnumMap<>(EstadoReserva.class);
        porEstado.put(EstadoReserva.CONFIRMADA, 3L);
        porEstado.put(EstadoReserva.CANCELADA, 1L);
        when(estadisticasUseCase.consultar(1L, FUTURO, FUTURO.plusDays(7))).thenReturn(new Estadisticas(FUTURO,
                FUTURO.plusDays(7), 4, porEstado, List.of(new OcupacionCancha(1L, 6))));

        mvc.perform(get("/api/reservas/estadisticas").header("X-Usuario-Id", "1")
                        .param("desde", "2026-12-01").param("hasta", "2026-12-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.desde").value("2026-12-01"))
                .andExpect(jsonPath("$.hasta").value("2026-12-08"))
                .andExpect(jsonPath("$.totalReservas").value(4))
                .andExpect(jsonPath("$.porEstado.CONFIRMADA").value(3))
                .andExpect(jsonPath("$.porEstado.CANCELADA").value(1))
                .andExpect(jsonPath("$.ocupacionPorCancha[0].canchaId").value(1))
                .andExpect(jsonPath("$.ocupacionPorCancha[0].horasReservadas").value(6));
    }



    @Test
    void unErrorInesperadoSeConvierteEnError500Interno() throws Exception {
        when(reservaUseCase.obtener(1L)).thenThrow(new IllegalStateException("boom"));

        mvc.perform(get("/api/reservas/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("ERROR_INTERNO"));
    }

    @Test
    void lasExcepcionesDeSpringMvcConservanSuStatus() throws Exception {
        mvc.perform(put("/api/reservas/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").value("METODO_NO_PERMITIDO"));

        mvc.perform(post("/api/reservas").contentType(MediaType.TEXT_PLAIN).content("hola"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error").value("TIPO_NO_SOPORTADO"));
    }
}
