package com.reservasapp.canchas.infraestructure.seed;

import com.reservasapp.canchas.domain.model.EstadoCancha;
import com.reservasapp.canchas.domain.model.TipoDeporte;
import com.reservasapp.canchas.infraestructure.driver_adapters.jpa_repository.CanchaData;
import com.reservasapp.canchas.infraestructure.driver_adapters.jpa_repository.CanchaJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CanchaSeedRunnerTest {

    @Mock
    private CanchaJpaRepository repository;
    @InjectMocks
    private CanchaSeedRunner runner;
    @Captor
    private ArgumentCaptor<List<CanchaData>> captor;

    @Test
    void run_tablaVacia_creaLasTresCanchasDelContrato() {
        when(repository.count()).thenReturn(0L);

        runner.run(null);

        verify(repository).saveAll(captor.capture());
        List<CanchaData> canchas = captor.getValue();
        assertEquals(3, canchas.size());

        CanchaData sintetica = canchas.get(0);
        assertEquals("Sintética 1", sintetica.getNombre());
        assertEquals(TipoDeporte.FUTBOL_5, sintetica.getTipoDeporte());
        assertFalse(sintetica.isTechada());
        assertEquals(LocalTime.of(6, 0), sintetica.getHoraApertura());
        assertEquals(LocalTime.of(22, 0), sintetica.getHoraCierre());
        assertEquals(50000L, sintetica.getPrecioPorHora());

        CanchaData coliseo = canchas.get(1);
        assertEquals("Coliseo", coliseo.getNombre());
        assertEquals(TipoDeporte.BALONCESTO, coliseo.getTipoDeporte());
        assertTrue(coliseo.isTechada());
        assertEquals(LocalTime.of(8, 0), coliseo.getHoraApertura());
        assertEquals(LocalTime.of(21, 0), coliseo.getHoraCierre());
        assertEquals(40000L, coliseo.getPrecioPorHora());

        CanchaData padel = canchas.get(2);
        assertEquals("Pádel 1", padel.getNombre());
        assertEquals(TipoDeporte.PADEL, padel.getTipoDeporte());
        assertFalse(padel.isTechada());
        assertEquals(LocalTime.of(7, 0), padel.getHoraApertura());
        assertEquals(60000L, padel.getPrecioPorHora());

        assertTrue(canchas.stream().allMatch(c -> c.getEstado() == EstadoCancha.ACTIVA));
    }

    @Test
    void run_tablaConDatos_noInsertaNada() {
        when(repository.count()).thenReturn(3L);

        runner.run(null);

        verify(repository, never()).saveAll(any());
    }
}
