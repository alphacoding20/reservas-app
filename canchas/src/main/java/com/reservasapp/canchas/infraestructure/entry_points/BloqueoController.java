package com.reservasapp.canchas.infraestructure.entry_points;

import com.reservasapp.canchas.domain.model.Bloqueo;
import com.reservasapp.canchas.domain.usecase.AutorizacionUseCase;
import com.reservasapp.canchas.domain.usecase.BloqueoUseCase;
import com.reservasapp.canchas.infraestructure.entry_points.dto.BloqueoDTO;
import com.reservasapp.canchas.infraestructure.entry_points.dto.BloqueoRequest;
import com.reservasapp.canchas.infraestructure.mapper.ConsultaDtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/canchas")
@RequiredArgsConstructor
public class BloqueoController {

    private final AutorizacionUseCase autorizacionUseCase;
    private final BloqueoUseCase bloqueoUseCase;

    @PostMapping("/{id}/bloqueos")
    public ResponseEntity<BloqueoDTO> crear(
            @RequestHeader(name = CanchaController.HEADER_USUARIO, required = false) Long adminId,
            @PathVariable("id") Long canchaId,
            @Valid @RequestBody BloqueoRequest request) {
        autorizacionUseCase.exigirAdmin(adminId);
        Bloqueo creado = bloqueoUseCase.crear(canchaId, request.fechaInicio(), request.fechaFin(), request.motivo());
        return ResponseEntity.status(HttpStatus.CREATED).body(ConsultaDtoMapper.toDto(creado));
    }

    @DeleteMapping("/{id}/bloqueos/{bloqueoId}")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(name = CanchaController.HEADER_USUARIO, required = false) Long adminId,
            @PathVariable("id") Long canchaId,
            @PathVariable("bloqueoId") Long bloqueoId) {
        autorizacionUseCase.exigirAdmin(adminId);
        bloqueoUseCase.eliminar(canchaId, bloqueoId);
        return ResponseEntity.noContent().build();
    }
}
