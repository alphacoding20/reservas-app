package com.reservasapp.auth.infraestructure.driver_adapters.jpa_repository;

import com.reservasapp.auth.domain.model.Usuario;
import com.reservasapp.auth.domain.model.gateway.UsuarioGateway;
import com.reservasapp.auth.infraestructure.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsuarioGatewayImpl implements UsuarioGateway {

    private final UsuarioJpaRepository repository;

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioData guardado = repository.save(UsuarioMapper.toData(usuario));
        return UsuarioMapper.toDomain(guardado);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return repository.findById(id).map(UsuarioMapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return repository.findByCorreo(correo).map(UsuarioMapper::toDomain);
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return repository.existsByCorreo(correo);
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
