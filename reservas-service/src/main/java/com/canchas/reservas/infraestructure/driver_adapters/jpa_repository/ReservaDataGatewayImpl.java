package com.canchas.reservas.infraestructure.driver_adapters.jpa_repository;

import com.canchas.reservas.domain.model.EstadoReserva;
import com.canchas.reservas.domain.model.Reserva;
import com.canchas.reservas.domain.model.gateway.ReservaGateway;
import com.canchas.reservas.infraestructure.mapper.ReservaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReservaDataGatewayImpl implements ReservaGateway {

    private static final long SIN_EXCLUSION = -1L;

    private final ReservaJpaRepository repository;
    private final ReservaMapper mapper;

    @Override
    public Reserva guardar(Reserva reserva) {
        return mapper.toReserva(repository.save(mapper.toData(reserva)));
    }

    @Override
    public Optional<Reserva> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toReserva);
    }

    @Override
    public List<Reserva> listarTodas() {
        return aReservas(repository.findAll(Sort.by("id")));
    }

    @Override
    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existeSolapamiento(Long canchaId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                      Long excluirReservaId) {
        long excluir = excluirReservaId == null ? SIN_EXCLUSION : excluirReservaId;
        return repository.contarSolapadas(canchaId, fecha, EstadoReserva.CONFIRMADA, horaInicio, horaFin, excluir) > 0;
    }

    @Override
    public long contarConfirmadasFuturas(Long usuarioId, LocalDateTime ahora) {
        return repository.contarFuturas(usuarioId, EstadoReserva.CONFIRMADA, ahora.toLocalDate(), ahora.toLocalTime());
    }

    @Override
    public List<Reserva> buscarPorUsuario(Long usuarioId) {
        return aReservas(repository.findByUsuarioIdOrderByFechaDescHoraInicioDesc(usuarioId));
    }

    @Override
    public List<Reserva> buscarConfirmadasPorFecha(LocalDate fecha) {
        return aReservas(repository.findByFechaAndEstadoOrderByHoraInicioAsc(fecha, EstadoReserva.CONFIRMADA));
    }

    @Override
    public List<Reserva> buscarConfirmadasPorCanchaYFecha(Long canchaId, LocalDate fecha) {
        return aReservas(repository.findByCanchaIdAndFechaAndEstadoOrderByHoraInicioAsc(
                canchaId, fecha, EstadoReserva.CONFIRMADA));
    }

    @Override
    public List<Reserva> buscarPorRangoFechas(LocalDate desde, LocalDate hasta) {
        return aReservas(repository.findByFechaBetween(desde, hasta));
    }

    private List<Reserva> aReservas(List<ReservaData> datos) {
        return datos.stream().map(mapper::toReserva).toList();
    }
}
