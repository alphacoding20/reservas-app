package com.reservasapp.auth.infraestructure.entry_points.dto;

import com.reservasapp.auth.domain.model.Rol;
import jakarta.validation.constraints.NotNull;

public record RolRequest(@NotNull Rol rol) {
}
