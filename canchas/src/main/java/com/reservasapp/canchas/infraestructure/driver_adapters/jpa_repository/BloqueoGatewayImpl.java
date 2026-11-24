package com.reservasapp.canchas.infraestructure.driver_adapters.jpa_repository;

import com.reservasapp.canchas.domain.model.Bloqueo;
import com.reservasapp.canchas.domain.model.gateway.BloqueoGateway;
import com.reservasapp.canchas.infraestructure.mapper.BloqueoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BloqueoGatewayImpl implements BloqueoGateway {

    private final BloqueoJpaRepository repository;

    @Override
    public Bloqueo guardar(Bloqueo bloqueo) {
        return BloqueoMapper.toDomain(repository.save(BloqueoMapper.toData(bloqueo)));
    }

    @Override
    public Optional<Bloqueo> buscarPorId(Long id) {
        return repository.findById(id).map(BloqueoMapper::toDomain);
    }

    @Override
    public List<Bloqueo> listarSolapados(Long canchaId, LocalDateTime desde, LocalDateTime hasta) {
        return repository
                .findByCanchaIdAndFechaInicioLessThanAndFechaFinGreaterThanOrderByFechaInicioAsc(
                        canchaId, hasta, desde)
                .stream()
                .map(BloqueoMapper::toDomain)
                .toList();
    }

    @Override
    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void eliminarPorCancha(Long canchaId) {
        repository.deleteByCanchaId(canchaId);
    }
}
