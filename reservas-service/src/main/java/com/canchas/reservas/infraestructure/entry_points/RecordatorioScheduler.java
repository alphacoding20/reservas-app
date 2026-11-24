package com.canchas.reservas.infraestructure.entry_points;

import com.canchas.reservas.domain.model.ResumenRecordatorios;
import com.canchas.reservas.domain.usecase.RecordatorioUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@EnableScheduling
@ConditionalOnProperty(name = "reservas.recordatorios.scheduler.enabled", havingValue = "true")
@RequiredArgsConstructor
public class RecordatorioScheduler {

    private static final Logger LOG = Logger.getLogger(RecordatorioScheduler.class.getName());

    private final RecordatorioUseCase recordatorioUseCase;
    private final Clock clock;

    @Scheduled(cron = "${reservas.recordatorios.cron}", zone = "America/Bogota")
    public void enviarRecordatoriosDeManana() {
        try {
            ResumenRecordatorios resumen = recordatorioUseCase.enviarParaFecha(LocalDate.now(clock).plusDays(1));
            LOG.info("Recordatorios programados enviados: " + resumen.recordatoriosEnviados());
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Falló el envío programado de recordatorios", e);
        }
    }
}
