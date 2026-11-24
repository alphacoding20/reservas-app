package com.reservasapp.canchas.domain.usecase;

import com.reservasapp.canchas.domain.exception.NegocioException;
import com.reservasapp.canchas.domain.model.Bloqueo;
import com.reservasapp.canchas.domain.model.Cancha;
import com.reservasapp.canchas.domain.model.FranjaBloqueada;
import com.reservasapp.canchas.domain.model.HorarioDia;
import com.reservasapp.canchas.domain.model.gateway.BloqueoGateway;
import com.reservasapp.canchas.domain.model.gateway.CanchaGateway;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class HorarioUseCase {

    static final LocalTime FIN_DEL_DIA = LocalTime.of(23, 59);

    private final CanchaGateway canchaGateway;
    private final BloqueoGateway bloqueoGateway;

    public HorarioUseCase(CanchaGateway canchaGateway, BloqueoGateway bloqueoGateway) {
        this.canchaGateway = canchaGateway;
        this.bloqueoGateway = bloqueoGateway;
    }

    public HorarioDia horario(Long canchaId, LocalDate fecha) {
        Cancha cancha = canchaGateway.buscarPorId(canchaId).orElseThrow(NegocioException::canchaNoEncontrada);
        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.plusDays(1).atStartOfDay();

        List<FranjaBloqueada> franjas = bloqueoGateway.listarSolapados(canchaId, inicioDia, finDia).stream()
                .map(bloqueo -> recortar(bloqueo, inicioDia, finDia))
                .toList();

        return new HorarioDia(canchaId, fecha, cancha.getEstado(),
                cancha.getHoraApertura(), cancha.getHoraCierre(), franjas);
    }

    private FranjaBloqueada recortar(Bloqueo bloqueo, LocalDateTime inicioDia, LocalDateTime finDia) {
        LocalTime horaInicio = bloqueo.getFechaInicio().isBefore(inicioDia)
                ? LocalTime.MIN
                : bloqueo.getFechaInicio().toLocalTime();
        LocalTime horaFin = bloqueo.getFechaFin().isBefore(finDia)
                ? bloqueo.getFechaFin().toLocalTime()
                : FIN_DEL_DIA;
        return new FranjaBloqueada(horaInicio, horaFin, bloqueo.getMotivo());
    }
}
