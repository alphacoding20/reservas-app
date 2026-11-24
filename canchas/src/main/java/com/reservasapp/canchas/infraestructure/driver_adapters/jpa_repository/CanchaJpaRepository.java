package com.reservasapp.canchas.infraestructure.driver_adapters.jpa_repository;

import com.reservasapp.canchas.domain.model.EstadoCancha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CanchaJpaRepository extends JpaRepository<CanchaData, Long> {

    List<CanchaData> findAllByOrderByIdAsc();

    List<CanchaData> findByEstadoOrderByIdAsc(EstadoCancha estado);
}
