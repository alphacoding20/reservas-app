package com.reservasapp.auth.infraestructure.entry_points.dto;

import com.reservasapp.auth.domain.model.Rol;

public record LoginResponse(Long id, String nombre, String correo, Rol rol) {
}
