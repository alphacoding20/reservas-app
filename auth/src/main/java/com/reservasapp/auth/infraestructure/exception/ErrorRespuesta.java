package com.reservasapp.auth.infraestructure.exception;

public record ErrorRespuesta(String timestamp, int status, String error, String mensaje, String ruta) {
}
