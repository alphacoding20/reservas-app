package com.canchas.reservas.infraestructure.entry_points;

import com.canchas.reservas.domain.exception.CodigoError;
import com.canchas.reservas.domain.exception.ReservasException;
import com.canchas.reservas.infraestructure.entry_points.dto.ReservaDtos.ErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class.getName());

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(ReservasException.class)
    public ResponseEntity<ErrorDTO> alManejarDominio(ReservasException e, HttpServletRequest request) {
        return responder(e.getCodigo().getStatus(), e.getCodigo().getCodigo(), e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> alValidarCuerpo(MethodArgumentNotValidException e, HttpServletRequest request) {
        String detalle = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Datos inválidos");
        return validacion("Datos inválidos: " + detalle, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> alLeerCuerpo(HttpMessageNotReadableException e, HttpServletRequest request) {
        return validacion("El cuerpo de la petición no es un JSON válido o tiene valores con formato incorrecto",
                request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorDTO> alFaltarParametro(MissingServletRequestParameterException e,
                                                      HttpServletRequest request) {
        return validacion("Falta el parámetro obligatorio '" + e.getParameterName() + "'", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDTO> alConvertirParametro(MethodArgumentTypeMismatchException e,
                                                         HttpServletRequest request) {
        return validacion("El valor del parámetro '" + e.getName() + "' tiene un formato inválido", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> alFallarAlgoInesperado(Exception e, HttpServletRequest request) {
        if (e instanceof ErrorResponse errorSpring && errorSpring.getStatusCode().is4xxClientError()) {
            int status = errorSpring.getStatusCode().value();
            String codigo = switch (status) {
                case 404 -> "RECURSO_NO_ENCONTRADO";
                case 405 -> "METODO_NO_PERMITIDO";
                case 415 -> "TIPO_NO_SOPORTADO";
                default -> CodigoError.VALIDACION.getCodigo();
            };
            return responder(status, codigo, errorSpring.getBody().getDetail(), request);
        }
        LOG.log(Level.SEVERE, "Error no controlado en " + request.getRequestURI(), e);
        return responder(CodigoError.ERROR_INTERNO.getStatus(), CodigoError.ERROR_INTERNO.getCodigo(),
                "Ocurrió un error interno", request);
    }

    private ResponseEntity<ErrorDTO> validacion(String mensaje, HttpServletRequest request) {
        return responder(CodigoError.VALIDACION.getStatus(), CodigoError.VALIDACION.getCodigo(), mensaje, request);
    }

    private ResponseEntity<ErrorDTO> responder(int status, String codigo, String mensaje, HttpServletRequest request) {
        ErrorDTO cuerpo = new ErrorDTO(LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS), status, codigo,
                mensaje, request.getRequestURI());
        return ResponseEntity.status(status).body(cuerpo);
    }
}
