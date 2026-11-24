package com.reservasapp.canchas.infraestructure.exception;

import com.reservasapp.canchas.domain.exception.NegocioException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Clock clock;

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErrorRespuesta> manejarNegocio(NegocioException ex, HttpServletRequest request) {
        return construir(ex.getStatus(), ex.getCodigo(), ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarValidacion(MethodArgumentNotValidException ex,
                                                            HttpServletRequest request) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return construir(400, "VALIDACION", detalle.isBlank() ? "Datos inválidos" : detalle, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> manejarCuerpoInvalido(HttpMessageNotReadableException ex,
                                                                HttpServletRequest request) {
        return construir(400, "VALIDACION", "El cuerpo de la petición es inválido", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuesta> manejarTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                              HttpServletRequest request) {
        return construir(400, "VALIDACION", "Valor inválido para el parámetro '" + ex.getName() + "'", request);
    }

    @ExceptionHandler(ServletException.class)
    public ResponseEntity<ErrorRespuesta> manejarServlet(ServletException ex, HttpServletRequest request) {
        int status = ex instanceof ErrorResponse errorResponse ? errorResponse.getStatusCode().value() : 500;
        String codigo = switch (status) {
            case 400 -> "VALIDACION";
            case 404 -> "RECURSO_NO_ENCONTRADO";
            case 405 -> "METODO_NO_PERMITIDO";
            case 415 -> "TIPO_NO_SOPORTADO";
            default -> "ERROR_INTERNO";
        };
        return construir(status, codigo, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> manejarGeneral(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return construir(500, "ERROR_INTERNO", "Ocurrió un error interno", request);
    }

    private ResponseEntity<ErrorRespuesta> construir(int status, String codigo, String mensaje,
                                                     HttpServletRequest request) {
        String timestamp = LocalDateTime.now(clock).withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        ErrorRespuesta cuerpo = new ErrorRespuesta(timestamp, status, codigo, mensaje, request.getRequestURI());
        return ResponseEntity.status(status).body(cuerpo);
    }
}
