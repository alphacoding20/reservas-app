package com.canchas.reservas.domain.usecase;

import com.canchas.reservas.domain.exception.ReservasException;
import com.canchas.reservas.domain.model.CanchaInfo;
import com.canchas.reservas.domain.model.UsuarioInfo;
import com.canchas.reservas.domain.model.gateway.CanchaServiceGateway;
import com.canchas.reservas.domain.model.gateway.UsuarioServiceGateway;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

class DatosExternos {

    private static final Logger LOG = Logger.getLogger(DatosExternos.class.getName());

    private final UsuarioServiceGateway usuarios;
    private final CanchaServiceGateway canchas;
    private final Map<Long, Optional<UsuarioInfo>> usuariosVistos = new HashMap<>();
    private final Map<Long, Optional<CanchaInfo>> canchasVistas = new HashMap<>();

    DatosExternos(UsuarioServiceGateway usuarios, CanchaServiceGateway canchas) {
        this.usuarios = usuarios;
        this.canchas = canchas;
    }

    Optional<UsuarioInfo> usuario(Long usuarioId) {
        return usuariosVistos.computeIfAbsent(usuarioId, id -> {
            try {
                return Optional.of(usuarios.validar(id));
            } catch (ReservasException e) {
                LOG.warning("No se pudo obtener el usuario " + id + ": " + e.getMessage());
                return Optional.empty();
            }
        });
    }

    Optional<CanchaInfo> cancha(Long canchaId) {
        return canchasVistas.computeIfAbsent(canchaId, id -> {
            try {
                return Optional.of(canchas.obtenerCancha(id));
            } catch (ReservasException e) {
                LOG.warning("No se pudo obtener la cancha " + id + ": " + e.getMessage());
                return Optional.empty();
            }
        });
    }
}
