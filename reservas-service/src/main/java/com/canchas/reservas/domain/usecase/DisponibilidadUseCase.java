package com.canchas.reservas.domain.usecase;

import com.canchas.reservas.domain.exception.CodigoError;
import com.canchas.reservas.domain.exception.ReservasException;
import com.canchas.reservas.domain.model.Disponibilidad;
import com.canchas.reservas.domain.model.Disponibilidad.FranjaDisponible;
import com.canchas.reservas.domain.model.EstadoCancha;
import com.canchas.reservas.domain.model.Franja;
import com.canchas.reservas.domain.model.HorarioCancha;
import com.canchas.reservas.domain.model.Reserva;
import com.canchas.reservas.domain.model.gateway.CanchaServiceGateway;
import com.canchas.reservas.domain.model.gateway.ReservaGateway;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DisponibilidadUseCase {

    private final ReservaGateway reservas;
    private final CanchaServiceGateway canchas;
    private final Clock clock;

    public DisponibilidadUseCase(ReservaGateway reservas, CanchaServiceGateway canchas, Clock clock) {
        this.reservas = reservas;
        this.canchas = canchas;
        this.clock = clock;
    }

    public Disponibilidad consultar(Long canchaId, LocalDate fecha) {
        if (canchaId == null || fecha == null) {
            throw new ReservasException(CodigoError.VALIDACION, "canchaId y fecha son obligatorios");
        }
        HorarioCancha horario = canchas.obtenerHorario(canchaId, fecha);
        if (horario.estado() != EstadoCancha.ACTIVA) {
            return new Disponibilidad(canchaId, fecha, List.of());
        }
        List<Reserva> confirmadas = reservas.buscarConfirmadasPorCanchaYFecha(canchaId, fecha);
        LocalDateTime ahora = LocalDateTime.now(clock);

        List<FranjaDisponible> franjas = new ArrayList<>();
        LocalTime inicio = horario.horaApertura();
        while (inicio.isBefore(horario.horaCierre())) {
            LocalTime fin = inicio.plusHours(1);
            if (!fin.isAfter(inicio) || fin.isAfter(horario.horaCierre())) {
                break;
            }
            franjas.add(new FranjaDisponible(inicio, fin, estaLibre(horario, confirmadas, fecha, inicio, fin, ahora)));
            inicio = fin;
        }
        return new Disponibilidad(canchaId, fecha, franjas);
    }

    private boolean estaLibre(HorarioCancha horario, List<Reserva> confirmadas, LocalDate fecha, LocalTime inicio,
                              LocalTime fin, LocalDateTime ahora) {
        boolean yaEmpezo = LocalDateTime.of(fecha, inicio).isBefore(ahora);
        boolean ocupada = confirmadas.stream().anyMatch(r -> Franja.chocan(r.horaInicio(), r.horaFin(), inicio, fin));
        return !yaEmpezo && !ocupada && !horario.chocaConBloqueo(inicio, fin);
    }
}
