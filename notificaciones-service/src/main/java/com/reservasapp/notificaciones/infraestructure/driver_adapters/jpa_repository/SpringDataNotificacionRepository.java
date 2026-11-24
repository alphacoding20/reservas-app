package com.reservasapp.notificaciones.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SpringDataNotificacionRepository extends JpaRepository<NotificacionEntity, Long> {

    List<NotificacionEntity> findByUsuarioIdOrderByFechaDescIdDesc(Long usuarioId);

    @Query("select n.tipo as tipo, n.estado as estado, count(n) as cantidad "
            + "from NotificacionEntity n group by n.tipo, n.estado")
    List<ConteoProjection> contarPorTipoYEstado();
}
