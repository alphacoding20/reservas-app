package com.reservasapp.auth.infraestructure.entry_points.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarClaveRequest(
        @NotBlank String claveActual,
        @NotBlank @Size(min = 6, max = 100) String claveNueva) {
}
