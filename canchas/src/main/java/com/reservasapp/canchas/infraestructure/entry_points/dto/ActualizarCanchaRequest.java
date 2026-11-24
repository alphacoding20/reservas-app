package com.reservasapp.canchas.infraestructure.entry_points.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.reservasapp.canchas.domain.model.EstadoCancha;
import com.reservasapp.canchas.domain.model.TipoDeporte;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ActualizarCanchaRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotNull TipoDeporte tipoDeporte,
        @NotBlank @Size(max = 200) String direccion,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitud,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitud,
        @NotNull @Positive Long precioPorHora,
        @NotNull Boolean techada,
        @NotBlank @Pattern(regexp = Patrones.HORA) String horaApertura,
        @NotBlank @Pattern(regexp = Patrones.HORA) String horaCierre,
        @NotNull EstadoCancha estado) {
}
