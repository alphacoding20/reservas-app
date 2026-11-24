package com.reservasapp.canchas.domain.usecase;

import com.reservasapp.canchas.domain.exception.NegocioException;
import com.reservasapp.canchas.domain.model.Cancha;
import com.reservasapp.canchas.domain.model.Festivo;
import com.reservasapp.canchas.domain.model.Tarifa;
import com.reservasapp.canchas.domain.model.gateway.CanchaGateway;
import com.reservasapp.canchas.domain.model.gateway.FestivoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarifaUseCaseTest {

    @Mock
    private CanchaGateway canchaGateway;
    @Mock
    private FestivoGateway festivoGateway;

    private TarifaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new TarifaUseCase(canchaGateway, festivoGateway, new BigDecimal("1.20"));
    }

    private void stubCancha(long precioBase) {
        when(canchaGateway.buscarPorId(1L)).thenReturn(Optional.of(
                Cancha.builder().id(1L).precioPorHora(precioBase).build()));
    }

    private void stubFestivos(Festivo... festivos) {
        when(festivoGateway.festivosDelAnio(2026)).thenReturn(List.of(festivos));
    }

    @Test
    void tarifa_festivoEntreSemana_aplicaRecargoYDevuelveElNombre() {
        stubCancha(50000);
        stubFestivos(new Festivo(LocalDate.of(2026, 12, 8), "Inmaculada Concepción"));

        Tarifa tarifa = useCase.tarifa(1L, LocalDate.of(2026, 12, 8));

        assertEquals(50000L, tarifa.precioBase());
        assertEquals(new BigDecimal("1.20"), tarifa.factor());
        assertEquals(60000L, tarifa.precioPorHora());
        assertTrue(tarifa.esFestivo());
        assertFalse(tarifa.esFinDeSemana());
        assertEquals("Inmaculada Concepción", tarifa.nombreFestivo());
    }

    @Test
    void tarifa_sabado_aplicaRecargoSinFestivo() {
        stubCancha(50000);
        stubFestivos();

        Tarifa tarifa = useCase.tarifa(1L, LocalDate.of(2026, 12, 12));

        assertEquals(60000L, tarifa.precioPorHora());
        assertTrue(tarifa.esFinDeSemana());
        assertFalse(tarifa.esFestivo());
        assertNull(tarifa.nombreFestivo());
    }

    @Test
    void tarifa_domingo_aplicaRecargo() {
        stubCancha(40000);
        stubFestivos();

        Tarifa tarifa = useCase.tarifa(1L, LocalDate.of(2026, 12, 13));

        assertEquals(48000L, tarifa.precioPorHora());
        assertTrue(tarifa.esFinDeSemana());
    }

    @Test
    void tarifa_diaHabilNormal_usaFactorUno() {
        stubCancha(50000);
        stubFestivos(new Festivo(LocalDate.of(2026, 12, 8), "Inmaculada Concepción"));

        Tarifa tarifa = useCase.tarifa(1L, LocalDate.of(2026, 12, 9));

        assertEquals(new BigDecimal("1.00"), tarifa.factor());
        assertEquals(50000L, tarifa.precioPorHora());
        assertFalse(tarifa.esFestivo());
        assertFalse(tarifa.esFinDeSemana());
        assertNull(tarifa.nombreFestivo());
    }

    @Test
    void tarifa_redondeaHalfUp() {
        stubCancha(33333);
        stubFestivos();

        // 33333 x 1.20 = 39999.6 -> 40000
        assertEquals(40000L, useCase.tarifa(1L, LocalDate.of(2026, 12, 12)).precioPorHora());
    }

    @Test
    void tarifa_canchaNoExiste_lanza404SinConsultarFestivos() {
        when(canchaGateway.buscarPorId(1L)).thenReturn(Optional.empty());

        NegocioException ex = assertThrows(NegocioException.class,
                () -> useCase.tarifa(1L, LocalDate.of(2026, 12, 8)));

        assertEquals("CANCHA_NO_ENCONTRADA", ex.getCodigo());
        verifyNoInteractions(festivoGateway);
    }

    @Test
    void festivos_delegaEnElGateway() {
        List<Festivo> festivos = List.of(new Festivo(LocalDate.of(2026, 1, 1), "Año Nuevo"));
        when(festivoGateway.festivosDelAnio(2026)).thenReturn(festivos);

        assertSame(festivos, useCase.festivos(2026));
    }
}
