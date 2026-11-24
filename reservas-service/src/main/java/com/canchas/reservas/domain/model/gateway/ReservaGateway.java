package com.canchas.reservas.domain.model.gateway;

import com.canchas.reservas.domain.model.Reserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservaGateway {

    Reserva guardar(Reserva reserva);

    Optional<Reserva> buscarPorId(Long id);

    List<Reserva> listarTodas();

    void eliminar(Long id);

    boolean existeSolapamiento(Long canchaId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                               Long excluirReservaId);

    long contarConfirmadasFuturas(Long usuarioId, LocalDateTime ahora);

    List<Reserva> buscarPorUsuario(Long usuarioId);

    List<Reserva> buscarConfirmadasPorFecha(LocalDate fecha);

    List<Reserva> buscarConfirmadasPorCanchaYFecha(Long canchaId, LocalDate fecha);

    List<Reserva> buscarPorRangoFechas(LocalDate desde, LocalDate hasta);
}
