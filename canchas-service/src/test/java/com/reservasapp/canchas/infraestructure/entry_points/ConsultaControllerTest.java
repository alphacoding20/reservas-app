package com.reservasapp.canchas.infraestructure.entry_points;

import com.reservasapp.canchas.domain.exception.NegocioException;
import com.reservasapp.canchas.domain.model.EstadoCancha;
import com.reservasapp.canchas.domain.model.Festivo;
import com.reservasapp.canchas.domain.model.FranjaBloqueada;
import com.reservasapp.canchas.domain.model.HorarioDia;
import com.reservasapp.canchas.domain.model.Tarifa;
import com.reservasapp.canchas.domain.usecase.HorarioUseCase;
import com.reservasapp.canchas.domain.usecase.TarifaUseCase;
import com.reservasapp.canchas.infraestructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ConsultaControllerTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 12, 8);

    @Mock
    private HorarioUseCase horarioUseCase;
    @Mock
    private TarifaUseCase tarifaUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-12-01T15:15:30Z"), ZoneId.of("America/Bogota"));
        mockMvc = MockMvcBuilders.standaloneSetup(new ConsultaController(horarioUseCase, tarifaUseCase))
                .setControllerAdvice(new GlobalExceptionHandler(clock))
                .build();
    }

    // ---------- /{id}/horario ----------

    @Test
    void horario_ok_devuelveLaEstructuraDelContrato() throws Exception {
        when(horarioUseCase.horario(1L, FECHA)).thenReturn(new HorarioDia(1L, FECHA, EstadoCancha.ACTIVA,
                LocalTime.of(6, 0), LocalTime.of(22, 0),
                List.of(new FranjaBloqueada(LocalTime.of(14, 0), LocalTime.of(16, 0), "Mantenimiento"))));

        mockMvc.perform(get("/api/canchas/1/horario").param("fecha", "2026-12-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canchaId").value(1))
                .andExpect(jsonPath("$.fecha").value("2026-12-08"))
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.horaApertura").value("06:00"))
                .andExpect(jsonPath("$.horaCierre").value("22:00"))
                .andExpect(jsonPath("$.bloqueos[0].horaInicio").value("14:00"))
                .andExpect(jsonPath("$.bloqueos[0].horaFin").value("16:00"))
                .andExpect(jsonPath("$.bloqueos[0].motivo").value("Mantenimiento"));
    }

    @Test
    void horario_canchaNoExiste_devuelve404() throws Exception {
        when(horarioUseCase.horario(9L, FECHA)).thenThrow(NegocioException.canchaNoEncontrada());

        mockMvc.perform(get("/api/canchas/9/horario").param("fecha", "2026-12-08"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("CANCHA_NO_ENCONTRADA"));
    }

    @Test
    void horario_sinFecha_devuelve400() throws Exception {
        mockMvc.perform(get("/api/canchas/1/horario"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));

        verifyNoInteractions(horarioUseCase);
    }

    @Test
    void horario_fechaMalFormada_devuelve400() throws Exception {
        mockMvc.perform(get("/api/canchas/1/horario").param("fecha", "08-12-2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));

        verifyNoInteractions(horarioUseCase);
    }

    // ---------- /{id}/tarifa ----------

    @Test
    void tarifa_ok_devuelveLaEstructuraDelContrato() throws Exception {
        when(tarifaUseCase.tarifa(1L, FECHA)).thenReturn(new Tarifa(1L, FECHA, 50000,
                new BigDecimal("1.20"), 60000, true, false, "Inmaculada Concepción"));

        mockMvc.perform(get("/api/canchas/1/tarifa").param("fecha", "2026-12-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canchaId").value(1))
                .andExpect(jsonPath("$.precioBase").value(50000))
                .andExpect(jsonPath("$.factor").value(1.20))
                .andExpect(jsonPath("$.precioPorHora").value(60000))
                .andExpect(jsonPath("$.esFestivo").value(true))
                .andExpect(jsonPath("$.esFinDeSemana").value(false))
                .andExpect(jsonPath("$.nombreFestivo").value("Inmaculada Concepción"));
    }

    @Test
    void tarifa_noFestivo_nombreFestivoSiempreEstaPresenteConNull() throws Exception {
        when(tarifaUseCase.tarifa(1L, FECHA)).thenReturn(new Tarifa(1L, FECHA, 50000,
                new BigDecimal("1.00"), 50000, false, false, null));

        mockMvc.perform(get("/api/canchas/1/tarifa").param("fecha", "2026-12-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreFestivo").hasJsonPath())
                .andExpect(jsonPath("$.nombreFestivo").isEmpty());
    }

    @Test
    void tarifa_canchaNoExiste_devuelve404() throws Exception {
        when(tarifaUseCase.tarifa(9L, FECHA)).thenThrow(NegocioException.canchaNoEncontrada());

        mockMvc.perform(get("/api/canchas/9/tarifa").param("fecha", "2026-12-08"))
                .andExpect(status().isNotFound());
    }

    @Test
    void tarifa_sinFecha_devuelve400() throws Exception {
        mockMvc.perform(get("/api/canchas/1/tarifa"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(tarifaUseCase);
    }

    // ---------- /festivos/{anio} ----------

    @Test
    void festivos_ok_devuelveArregloDeFechaYNombre() throws Exception {
        when(tarifaUseCase.festivos(2026)).thenReturn(List.of(
                new Festivo(LocalDate.of(2026, 1, 1), "Año Nuevo"),
                new Festivo(LocalDate.of(2026, 12, 8), "Inmaculada Concepción")));

        mockMvc.perform(get("/api/canchas/festivos/2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fecha").value("2026-01-01"))
                .andExpect(jsonPath("$[0].nombre").value("Año Nuevo"));
    }

    @Test
    void festivos_cuandoNagerFalla_devuelveArregloVacio() throws Exception {
        when(tarifaUseCase.festivos(2026)).thenReturn(List.of());

        mockMvc.perform(get("/api/canchas/festivos/2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void festivos_anioNoNumerico_devuelve400() throws Exception {
        mockMvc.perform(get("/api/canchas/festivos/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"));
    }
}
