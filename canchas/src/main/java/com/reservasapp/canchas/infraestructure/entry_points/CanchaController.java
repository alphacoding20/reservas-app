package com.reservasapp.canchas.infraestructure.entry_points;

import com.reservasapp.canchas.domain.model.Cancha;
import com.reservasapp.canchas.domain.model.TipoDeporte;
import com.reservasapp.canchas.domain.usecase.AutorizacionUseCase;
import com.reservasapp.canchas.domain.usecase.CanchaUseCase;
import com.reservasapp.canchas.infraestructure.entry_points.dto.ActualizarCanchaRequest;
import com.reservasapp.canchas.infraestructure.entry_points.dto.CanchaDTO;
import com.reservasapp.canchas.infraestructure.entry_points.dto.CrearCanchaRequest;
import com.reservasapp.canchas.infraestructure.mapper.CanchaDtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/canchas")
@RequiredArgsConstructor
public class CanchaController {

    static final String HEADER_USUARIO = "X-Usuario-Id";

    private final AutorizacionUseCase autorizacionUseCase;
    private final CanchaUseCase canchaUseCase;

    @PostMapping
    public ResponseEntity<CanchaDTO> crear(
            @RequestHeader(name = HEADER_USUARIO, required = false) Long adminId,
            @Valid @RequestBody CrearCanchaRequest request) {
        autorizacionUseCase.exigirAdmin(adminId);
        Cancha creada = canchaUseCase.crear(CanchaDtoMapper.toCancha(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(CanchaDtoMapper.toDto(creada));
    }

    @GetMapping
    public ResponseEntity<List<CanchaDTO>> listar() {
        return ResponseEntity.ok(canchaUseCase.listar().stream().map(CanchaDtoMapper::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CanchaDTO> obtener(@PathVariable("id") Long id) {
        return ResponseEntity.ok(CanchaDtoMapper.toDto(canchaUseCase.obtener(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CanchaDTO> actualizar(
            @RequestHeader(name = HEADER_USUARIO, required = false) Long adminId,
            @PathVariable("id") Long id,
            @Valid @RequestBody ActualizarCanchaRequest request) {
        autorizacionUseCase.exigirAdmin(adminId);
        Cancha actualizada = canchaUseCase.actualizar(id, CanchaDtoMapper.toCancha(request));
        return ResponseEntity.ok(CanchaDtoMapper.toDto(actualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(name = HEADER_USUARIO, required = false) Long adminId,
            @PathVariable("id") Long id) {
        autorizacionUseCase.exigirAdmin(adminId);
        canchaUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<CanchaDTO>> buscar(
            @RequestParam(name = "deporte", required = false) TipoDeporte deporte,
            @RequestParam(name = "techada", required = false) Boolean techada,
            @RequestParam(name = "precioMax", required = false) Long precioMax) {
        return ResponseEntity.ok(canchaUseCase.buscar(deporte, techada, precioMax).stream()
                .map(CanchaDtoMapper::toDto)
                .toList());
    }
}
