package com.reservasapp.canchas.infraestructure.exception;

public record ErrorRespuesta(String timestamp, int status, String error, String mensaje, String ruta) {
}
