package com.reservasapp.auth.infraestructure.entry_points;

import com.reservasapp.auth.domain.model.Usuario;
import com.reservasapp.auth.domain.usecase.UsuarioUseCase;
import com.reservasapp.auth.infraestructure.entry_points.dto.ActualizarUsuarioRequest;
import com.reservasapp.auth.infraestructure.entry_points.dto.CambiarClaveRequest;
import com.reservasapp.auth.infraestructure.entry_points.dto.CrearUsuarioRequest;
import com.reservasapp.auth.infraestructure.entry_points.dto.EstadoRequest;
import com.reservasapp.auth.infraestructure.entry_points.dto.LoginRequest;
import com.reservasapp.auth.infraestructure.entry_points.dto.LoginResponse;
import com.reservasapp.auth.infraestructure.entry_points.dto.RolRequest;
import com.reservasapp.auth.infraestructure.entry_points.dto.UsuarioDTO;
import com.reservasapp.auth.infraestructure.entry_points.dto.ValidarResponse;
import com.reservasapp.auth.infraestructure.mapper.UsuarioDtoMapper;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UsuarioController {

    private static final String HEADER_USUARIO = "X-Usuario-Id";

    private final UsuarioUseCase usuarioUseCase;

    @PostMapping("/save")
    public ResponseEntity<UsuarioDTO> guardar(@Valid @RequestBody CrearUsuarioRequest request) {
        Usuario creado = usuarioUseCase.registrar(UsuarioDtoMapper.toUsuario(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioDtoMapper.toDto(creado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> obtener(@PathVariable("id") Long id) {
        return ResponseEntity.ok(UsuarioDtoMapper.toDto(usuarioUseCase.obtener(id)));
    }

    @PutMapping("/update")
    public ResponseEntity<UsuarioDTO> actualizar(@Valid @RequestBody ActualizarUsuarioRequest request) {
        Usuario actualizado = usuarioUseCase.actualizar(UsuarioDtoMapper.toUsuario(request));
        return ResponseEntity.ok(UsuarioDtoMapper.toDto(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        usuarioUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = usuarioUseCase.login(request.correo(), request.clave());
        return ResponseEntity.ok(UsuarioDtoMapper.toLogin(usuario));
    }

    @PutMapping("/{id}/clave")
    public ResponseEntity<Void> cambiarClave(@PathVariable("id") Long id,
                                             @Valid @RequestBody CambiarClaveRequest request) {
        usuarioUseCase.cambiarClave(id, request.claveActual(), request.claveNueva());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/correo/{correo}")
    public ResponseEntity<UsuarioDTO> buscarPorCorreo(@PathVariable("correo") String correo) {
        return ResponseEntity.ok(UsuarioDtoMapper.toDto(usuarioUseCase.buscarPorCorreo(correo)));
    }

    @PutMapping("/{id}/rol")
    public ResponseEntity<UsuarioDTO> cambiarRol(
            @RequestHeader(name = HEADER_USUARIO, required = false) Long adminId,
            @PathVariable("id") Long id,
            @Valid @RequestBody RolRequest request) {
        usuarioUseCase.exigirAdmin(adminId);
        return ResponseEntity.ok(UsuarioDtoMapper.toDto(usuarioUseCase.cambiarRol(id, request.rol())));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<UsuarioDTO> cambiarEstado(
            @RequestHeader(name = HEADER_USUARIO, required = false) Long adminId,
            @PathVariable("id") Long id,
            @Valid @RequestBody EstadoRequest request) {
        usuarioUseCase.exigirAdmin(adminId);
        return ResponseEntity.ok(UsuarioDtoMapper.toDto(usuarioUseCase.cambiarEstado(id, request.activo())));
    }

    @GetMapping("/{id}/validar")
    public ResponseEntity<ValidarResponse> validar(@PathVariable("id") Long id) {
        return ResponseEntity.ok(UsuarioDtoMapper.toValidar(usuarioUseCase.validar(id)));
    }
}
