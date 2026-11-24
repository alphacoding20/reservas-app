package com.canchas.reservas.infraestructure.driver_adapters.jpa_repository;

import com.canchas.reservas.domain.model.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservaJpaRepository extends JpaRepository<ReservaData, Long> {

    @Query("select count(r) from ReservaData r "
            + "where r.canchaId = :canchaId and r.fecha = :fecha and r.estado = :estado "
            + "and r.horaInicio < :fin and r.horaFin > :inicio and r.id <> :excluirId")
    long contarSolapadas(@Param("canchaId") Long canchaId, @Param("fecha") LocalDate fecha,
                         @Param("estado") EstadoReserva estado, @Param("inicio") LocalTime inicio,
                         @Param("fin") LocalTime fin, @Param("excluirId") Long excluirId);

    @Query("select count(r) from ReservaData r "
            + "where r.usuarioId = :usuarioId and r.estado = :estado "
            + "and (r.fecha > :hoy or (r.fecha = :hoy and r.horaInicio > :hora))")
    long contarFuturas(@Param("usuarioId") Long usuarioId, @Param("estado") EstadoReserva estado,
                       @Param("hoy") LocalDate hoy, @Param("hora") LocalTime hora);

    List<ReservaData> findByUsuarioIdOrderByFechaDescHoraInicioDesc(Long usuarioId);

    List<ReservaData> findByFechaAndEstadoOrderByHoraInicioAsc(LocalDate fecha, EstadoReserva estado);

    List<ReservaData> findByCanchaIdAndFechaAndEstadoOrderByHoraInicioAsc(Long canchaId, LocalDate fecha,
                                                                          EstadoReserva estado);

    List<ReservaData> findByFechaBetween(LocalDate desde, LocalDate hasta);
}
