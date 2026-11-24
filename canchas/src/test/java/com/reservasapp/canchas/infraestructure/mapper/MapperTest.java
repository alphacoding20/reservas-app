package com.reservasapp.canchas.infraestructure.mapper;

import com.reservasapp.canchas.domain.model.Bloqueo;
import com.reservasapp.canchas.domain.model.Cancha;
import com.reservasapp.canchas.domain.model.EstadoCancha;
import com.reservasapp.canchas.domain.model.Festivo;
import com.reservasapp.canchas.domain.model.FranjaBloqueada;
import com.reservasapp.canchas.domain.model.HorarioDia;
import com.reservasapp.canchas.domain.model.Tarifa;
import com.reservasapp.canchas.domain.model.TipoDeporte;
import com.reservasapp.canchas.infraestructure.driver_adapters.jpa_repository.BloqueoData;
import com.reservasapp.canchas.infraestructure.driver_adapters.jpa_repository.CanchaData;
import com.reservasapp.canchas.infraestructure.entry_points.dto.ActualizarCanchaRequest;
import com.reservasapp.canchas.infraestructure.entry_points.dto.BloqueoDTO;
import com.reservasapp.canchas.infraestructure.entry_points.dto.CanchaDTO;
import com.reservasapp.canchas.infraestructure.entry_points.dto.CrearCanchaRequest;
import com.reservasapp.canchas.infraestructure.entry_points.dto.FestivoDTO;
import com.reservasapp.canchas.infraestructure.entry_points.dto.HorarioDTO;
import com.reservasapp.canchas.infraestructure.entry_points.dto.TarifaDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapperTest {

    private Cancha cancha() {
        return Cancha.builder().id(1L).nombre("Sintética 1").tipoDeporte(TipoDeporte.FUTBOL_5)
                .direccion("Calle 1").latitud(4.33).longitud(-74.36).precioPorHora(50000L).techada(true)
                .horaApertura(LocalTime.of(6, 0)).horaCierre(LocalTime.of(22, 0))
                .estado(EstadoCancha.ACTIVA).build();
    }

    // ---------- Formatos ----------

    @Test
    void formatos_siguenElContrato() {
        assertEquals("06:00", Formatos.hora(LocalTime.of(6, 0)));
        assertEquals("2026-12-08", Formatos.fecha(LocalDate.of(2026, 12, 8)));
        assertEquals("2026-12-08T14:00:00", Formatos.fechaHora(LocalDateTime.of(2026, 12, 8, 14, 0)));
    }

    // ---------- Entidades ----------

    @Test
    void canchaMapper_ida_y_vuelta_conservaTodosLosCampos() {
        CanchaData data = CanchaMapper.toData(cancha());
        Cancha vuelta = CanchaMapper.toDomain(data);

        assertEquals(1L, data.getId());
        assertEquals(TipoDeporte.FUTBOL_5, data.getTipoDeporte());
        assertTrue(data.isTechada());
        assertEquals(LocalTime.of(22, 0), data.getHoraCierre());
        assertEquals("Sintética 1", vuelta.getNombre());
        assertEquals(4.33, vuelta.getLatitud());
        assertEquals(50000L, vuelta.getPrecioPorHora());
        assertEquals(EstadoCancha.ACTIVA, vuelta.getEstado());
    }

    @Test
    void canchaMapper_conNulos_devuelveNulo() {
        assertNull(CanchaMapper.toDomain(null));
        assertNull(CanchaMapper.toData(null));
    }

    @Test
    void bloqueoMapper_ida_y_vuelta_conservaTodosLosCampos() {
        Bloqueo bloqueo = Bloqueo.builder().id(3L).canchaId(1L)
                .fechaInicio(LocalDateTime.of(2026, 12, 8, 14, 0))
                .fechaFin(LocalDateTime.of(2026, 12, 8, 16, 0)).motivo("Mantenimiento").build();

        BloqueoData data = BloqueoMapper.toData(bloqueo);
        Bloqueo vuelta = BloqueoMapper.toDomain(data);

        assertEquals(3L, data.getId());
        assertEquals(1L, data.getCanchaId());
        assertEquals("Mantenimiento", vuelta.getMotivo());
        assertEquals(LocalDateTime.of(2026, 12, 8, 16, 0), vuelta.getFechaFin());
    }

    @Test
    void bloqueoMapper_conNulos_devuelveNulo() {
        assertNull(BloqueoMapper.toDomain(null));
        assertNull(BloqueoMapper.toData(null));
    }

    // ---------- DTO de canchas ----------

    @Test
    void canchaDtoMapper_crearRequest_aDominio_sinIdNiEstado() {
        Cancha resultado = CanchaDtoMapper.toCancha(new CrearCanchaRequest("Pádel 1", TipoDeporte.PADEL,
                "Calle 2", 4.3, -74.3, 60000L, false, "07:00", "22:00"));

        assertNull(resultado.getId());
        assertNull(resultado.getEstado());
        assertEquals(LocalTime.of(7, 0), resultado.getHoraApertura());
        assertEquals(LocalTime.of(22, 0), resultado.getHoraCierre());
        assertFalse(resultado.isTechada());
    }

    @Test
    void canchaDtoMapper_actualizarRequest_aDominio_conEstado() {
        Cancha resultado = CanchaDtoMapper.toCancha(new ActualizarCanchaRequest("Pádel 1", TipoDeporte.PADEL,
                "Calle 2", 4.3, -74.3, 60000L, true, "07:00", "22:00", EstadoCancha.MANTENIMIENTO));

        assertEquals(EstadoCancha.MANTENIMIENTO, resultado.getEstado());
        assertTrue(resultado.isTechada());
        assertEquals(60000L, resultado.getPrecioPorHora());
    }

    @Test
    void canchaDtoMapper_toDto_formateaLasHoras() {
        CanchaDTO dto = CanchaDtoMapper.toDto(cancha());

        assertEquals(1L, dto.id());
        assertEquals("06:00", dto.horaApertura());
        assertEquals("22:00", dto.horaCierre());
        assertEquals(EstadoCancha.ACTIVA, dto.estado());
        assertTrue(dto.techada());
    }

    // ---------- DTO de consultas ----------

    @Test
    void consultaDtoMapper_horario() {
        HorarioDTO dto = ConsultaDtoMapper.toDto(new HorarioDia(1L, LocalDate.of(2026, 12, 8),
                EstadoCancha.ACTIVA, LocalTime.of(6, 0), LocalTime.of(22, 0),
                List.of(new FranjaBloqueada(LocalTime.of(14, 0), LocalTime.of(16, 0), "Mantenimiento"))));

        assertEquals("2026-12-08", dto.fecha());
        assertEquals("06:00", dto.horaApertura());
        assertEquals(1, dto.bloqueos().size());
        assertEquals("14:00", dto.bloqueos().get(0).horaInicio());
        assertEquals("16:00", dto.bloqueos().get(0).horaFin());
        assertEquals("Mantenimiento", dto.bloqueos().get(0).motivo());
    }

    @Test
    void consultaDtoMapper_tarifa() {
        TarifaDTO dto = ConsultaDtoMapper.toDto(new Tarifa(1L, LocalDate.of(2026, 12, 8), 50000,
                new BigDecimal("1.20"), 60000, true, false, "Inmaculada Concepción"));

        assertEquals("2026-12-08", dto.fecha());
        assertEquals(new BigDecimal("1.20"), dto.factor());
        assertEquals(60000L, dto.precioPorHora());
        assertTrue(dto.esFestivo());
        assertFalse(dto.esFinDeSemana());
        assertEquals("Inmaculada Concepción", dto.nombreFestivo());
    }

    @Test
    void consultaDtoMapper_bloqueo() {
        BloqueoDTO dto = ConsultaDtoMapper.toDto(Bloqueo.builder().id(3L).canchaId(1L)
                .fechaInicio(LocalDateTime.of(2026, 12, 8, 14, 0))
                .fechaFin(LocalDateTime.of(2026, 12, 8, 16, 0)).motivo("Mantenimiento").build());

        assertEquals(3L, dto.id());
        assertEquals(1L, dto.canchaId());
        assertEquals("2026-12-08T14:00:00", dto.fechaInicio());
        assertEquals("2026-12-08T16:00:00", dto.fechaFin());
    }

    @Test
    void consultaDtoMapper_festivo() {
        FestivoDTO dto = ConsultaDtoMapper.toDto(new Festivo(LocalDate.of(2026, 1, 1), "Año Nuevo"));

        assertEquals("2026-01-01", dto.fecha());
        assertEquals("Año Nuevo", dto.nombre());
    }
}
