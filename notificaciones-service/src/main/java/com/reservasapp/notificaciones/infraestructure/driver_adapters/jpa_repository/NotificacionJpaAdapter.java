package com.reservasapp.notificaciones.infraestructure.driver_adapters.jpa_repository;

import com.reservasapp.notificaciones.domain.model.ConteoNotificacion;
import com.reservasapp.notificaciones.domain.model.Notificacion;
import com.reservasapp.notificaciones.domain.model.gateway.NotificacionGateway;
import com.reservasapp.notificaciones.infraestructure.mapper.NotificacionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificacionJpaAdapter implements NotificacionGateway {

    private final SpringDataNotificacionRepository repository;
    private final NotificacionMapper mapper;

    @Override
    public Notificacion guardar(Notificacion notificacion) {
        return mapper.toDomain(repository.save(mapper.toEntity(notificacion)));
    }

    @Override
    public Optional<Notificacion> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Notificacion> buscarPorUsuario(Long usuarioId) {
        return repository.findByUsuarioIdOrderByFechaDescIdDesc(usuarioId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existePorId(Long id) {
        return repository.existsById(id);
    }

    @Override
    public void eliminarPorId(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<ConteoNotificacion> contarPorTipoYEstado() {
        return repository.contarPorTipoYEstado().stream()
                .map(c -> new ConteoNotificacion(c.getTipo(), c.getEstado(), c.getCantidad()))
                .toList();
    }
}
