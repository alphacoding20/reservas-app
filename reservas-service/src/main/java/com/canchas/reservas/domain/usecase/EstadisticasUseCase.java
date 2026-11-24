package com.canchas.reservas.domain.usecase;

import com.canchas.reservas.domain.exception.CodigoError;
import com.canchas.reservas.domain.exception.ReservasException;
import com.canchas.reservas.domain.model.Estadisticas;
import com.canchas.reservas.domain.model.Estadisticas.OcupacionCancha;
import com.canchas.reservas.domain.model.EstadoReserva;
import com.canchas.reservas.domain.model.Reserva;
import com.canchas.reservas.domain.model.gateway.ReservaGateway;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EstadisticasUseCase {

    private final ReservaGateway reservas;
    private final ValidadorAdmin admin;

    public EstadisticasUseCase(ReservaGateway reservas, ValidadorAdmin admin) {
        this.reservas = reservas;
        this.admin = admin;
    }

    public Estadisticas consultar(Long adminId, LocalDate desde, LocalDate hasta) {
        admin.exigirAdmin(adminId);
        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            throw new ReservasException(CodigoError.VALIDACION, "Rango inválido: 'desde' debe ser menor o igual a 'hasta'");
        }
        List<Reserva> delRango = reservas.buscarPorRangoFechas(desde, hasta);

        Map<EstadoReserva, Long> porEstado = new EnumMap<>(EstadoReserva.class);
        for (EstadoReserva estado : EstadoReserva.values()) {
            porEstado.put(estado, 0L);
        }
        Map<Long, Long> horasPorCancha = new TreeMap<>();
        for (Reserva reserva : delRango) {
            porEstado.merge(reserva.estado(), 1L, Long::sum);
            if (reserva.estado() != EstadoReserva.CANCELADA) {
                horasPorCancha.merge(reserva.canchaId(), reserva.horas(), Long::sum);
            }
        }
        List<OcupacionCancha> ocupacion = horasPorCancha.entrySet().stream()
                .map(e -> new OcupacionCancha(e.getKey(), e.getValue()))
                .toList();
        return new Estadisticas(desde, hasta, delRango.size(), porEstado, ocupacion);
    }
}
