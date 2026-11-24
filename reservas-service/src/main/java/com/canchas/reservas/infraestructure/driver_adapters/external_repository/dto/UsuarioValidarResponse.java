package com.canchas.reservas.infraestructure.driver_adapters.external_repository.dto;

import com.canchas.reservas.domain.model.Rol;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioValidarResponse(Long id, String nombre, String correo, Rol rol, Boolean activo,
                                     String telegramChatId) {
}
