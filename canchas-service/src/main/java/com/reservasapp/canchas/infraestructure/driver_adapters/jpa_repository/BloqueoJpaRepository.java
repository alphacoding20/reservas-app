package com.reservasapp.canchas.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface BloqueoJpaRepository extends JpaRepository<BloqueoData, Long> {

    List<BloqueoData> findByCanchaIdAndFechaInicioLessThanAndFechaFinGreaterThanOrderByFechaInicioAsc(
            Long canchaId, LocalDateTime hasta, LocalDateTime desde);

    @Transactional
    void deleteByCanchaId(Long canchaId);
}
