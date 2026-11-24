package com.reservasapp.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    private Long id;
    private String nombre;
    private String correo;
    private String clave;
    private Integer edad;
    private String numeroTelefonico;
    private Rol rol;
    private boolean activo;
    private String telegramChatId;
}
