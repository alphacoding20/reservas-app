package com.reservasapp.canchas.infraestructure.entry_points;

import com.reservasapp.canchas.domain.usecase.HorarioUseCase;
import com.reservasapp.canchas.domain.usecase.TarifaUseCase;
import com.reservasapp.canchas.infraestructure.entry_points.dto.FestivoDTO;
import com.reservasapp.canchas.infraestructure.entry_points.dto.HorarioDTO;
import com.reservasapp.canchas.infraestructure.entry_points.dto.TarifaDTO;
import com.reservasapp.canchas.infraestructure.mapper.ConsultaDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/canchas")
@RequiredArgsConstructor
public class ConsultaController {

    private final HorarioUseCase horarioUseCase;
    private final TarifaUseCase tarifaUseCase;

    @GetMapping("/{id}/horario")
    public ResponseEntity<HorarioDTO> horario(
            @PathVariable("id") Long canchaId,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(ConsultaDtoMapper.toDto(horarioUseCase.horario(canchaId, fecha)));
    }

    @GetMapping("/{id}/tarifa")
    public ResponseEntity<TarifaDTO> tarifa(
            @PathVariable("id") Long canchaId,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(ConsultaDtoMapper.toDto(tarifaUseCase.tarifa(canchaId, fecha)));
    }

    @GetMapping("/festivos/{anio}")
    public ResponseEntity<List<FestivoDTO>> festivos(@PathVariable("anio") int anio) {
        return ResponseEntity.ok(tarifaUseCase.festivos(anio).stream().map(ConsultaDtoMapper::toDto).toList());
    }
}
