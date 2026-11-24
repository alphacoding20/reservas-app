package com.canchas.reservas.infraestructure.entry_points;

import com.canchas.reservas.domain.model.ResumenRecordatorios;
import com.canchas.reservas.domain.usecase.RecordatorioUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static com.canchas.reservas.Fixtures.CLOCK;
import static com.canchas.reservas.Fixtures.HOY;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordatorioSchedulerTest {

    @Mock
    RecordatorioUseCase useCase;

    @Test
    void enviaLosRecordatoriosDeLasReservasDeManana() {
        when(useCase.enviarParaFecha(HOY.plusDays(1))).thenReturn(new ResumenRecordatorios(HOY.plusDays(1), 3));

        new RecordatorioScheduler(useCase, CLOCK).enviarRecordatoriosDeManana();

        verify(useCase).enviarParaFecha(HOY.plusDays(1));
    }

    @Test
    void unFalloNoDetieneElTemporizador() {
        when(useCase.enviarParaFecha(HOY.plusDays(1))).thenThrow(new IllegalStateException("boom"));

        assertDoesNotThrow(() -> new RecordatorioScheduler(useCase, CLOCK).enviarRecordatoriosDeManana());
    }
}
