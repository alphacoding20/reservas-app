package com.reservasapp.canchas.infraestructure.driver_adapters.jpa_repository;

import com.reservasapp.canchas.domain.model.Cancha;
import com.reservasapp.canchas.domain.model.EstadoCancha;
import com.reservasapp.canchas.domain.model.gateway.CanchaGateway;
import com.reservasapp.canchas.infraestructure.mapper.CanchaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CanchaGatewayImpl implements CanchaGateway {

    private final CanchaJpaRepository repository;

    @Override
    public Cancha guardar(Cancha cancha) {
        return CanchaMapper.toDomain(repository.save(CanchaMapper.toData(cancha)));
    }

    @Override
    public Optional<Cancha> buscarPorId(Long id) {
        return repository.findById(id).map(CanchaMapper::toDomain);
    }

    @Override
    public List<Cancha> listar() {
        return repository.findAllByOrderByIdAsc().stream().map(CanchaMapper::toDomain).toList();
    }

    @Override
    public List<Cancha> listarPorEstado(EstadoCancha estado) {
        return repository.findByEstadoOrderByIdAsc(estado).stream().map(CanchaMapper::toDomain).toList();
    }

    @Override
    public boolean existePorId(Long id) {
        return repository.existsById(id);
    }

    @Override
    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
