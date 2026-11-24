package com.reservasapp.canchas.infraestructure.seed;

import com.reservasapp.canchas.domain.model.EstadoCancha;
import com.reservasapp.canchas.domain.model.TipoDeporte;
import com.reservasapp.canchas.infraestructure.driver_adapters.jpa_repository.CanchaData;
import com.reservasapp.canchas.infraestructure.driver_adapters.jpa_repository.CanchaJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CanchaSeedRunner implements ApplicationRunner {

    private final CanchaJpaRepository repository;

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }
        CanchaData sintetica = CanchaData.builder()
                .nombre("Sintética 1")
                .tipoDeporte(TipoDeporte.FUTBOL_5)
                .direccion("Fusagasugá, Cundinamarca")
                .latitud(4.3369)
                .longitud(-74.3639)
                .precioPorHora(50000L)
                .techada(false)
                .horaApertura(LocalTime.of(6, 0))
                .horaCierre(LocalTime.of(22, 0))
                .estado(EstadoCancha.ACTIVA)
                .build();
        CanchaData coliseo = CanchaData.builder()
                .nombre("Coliseo")
                .tipoDeporte(TipoDeporte.BALONCESTO)
                .direccion("Fusagasugá, Cundinamarca")
                .latitud(4.3382)
                .longitud(-74.3655)
                .precioPorHora(40000L)
                .techada(true)
                .horaApertura(LocalTime.of(8, 0))
                .horaCierre(LocalTime.of(21, 0))
                .estado(EstadoCancha.ACTIVA)
                .build();
        CanchaData padel = CanchaData.builder()
                .nombre("Pádel 1")
                .tipoDeporte(TipoDeporte.PADEL)
                .direccion("Fusagasugá, Cundinamarca")
                .latitud(4.3355)
                .longitud(-74.3621)
                .precioPorHora(60000L)
                .techada(false)
                .horaApertura(LocalTime.of(7, 0))
                .horaCierre(LocalTime.of(22, 0))
                .estado(EstadoCancha.ACTIVA)
                .build();
        repository.saveAll(List.of(sintetica, coliseo, padel));
        log.info("Datos semilla de canchas creados (ids 1, 2 y 3)");
    }
}
