package com.reservasapp.notificaciones.infraestructure.entry_points;

import com.reservasapp.notificaciones.domain.model.NotificacionNoEncontradaException;
import com.reservasapp.notificaciones.domain.model.ValidacionException;
import com.reservasapp.notificaciones.domain.model.YaEnviadaException;
import com.reservasapp.notificaciones.infraestructure.entry_points.dto.ErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Todo error devuelve {timestamp, status, error, mensaje, ruta} (contrato 2.4). Los consumidores deciden
 * por status + error, nunca por el texto de mensaje. Extiende ResponseEntityExceptionHandler para que
 * tambien los errores propios de Spring MVC (JSON malformado, tipo incorrecto, 405...) usen el mismo formato.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final Clock clock;

    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<ErrorDTO> validacion(ValidacionException e, HttpServletRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST, "VALIDACION", e.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(NotificacionNoEncontradaException.class)
    public ResponseEntity<ErrorDTO> noEncontrada(NotificacionNoEncontradaException e, HttpServletRequest request) {
        return respuesta(HttpStatus.NOT_FOUND, "NOTIFICACION_NO_ENCONTRADA", e.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(YaEnviadaException.class)
    public ResponseEntity<ErrorDTO> yaEnviada(YaEnviadaException e, HttpServletRequest request) {
        return respuesta(HttpStatus.CONFLICT, "YA_ENVIADA", e.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> inesperado(Exception e, HttpServletRequest request) {
        log.error("Error no controlado en {}", request.getRequestURI(), e);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrió un error interno", request.getRequestURI());
    }


    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        int status = statusCode.value();
        String codigo;
        String mensaje;
        if (status == 400) {
            codigo = "VALIDACION";
            mensaje = "Petición inválida: revisa el cuerpo, los parámetros y los formatos";
        } else if (status == 404) {
            codigo = "RECURSO_NO_ENCONTRADO";
            mensaje = "La ruta solicitada no existe";
        } else if (status == 405) {
            codigo = "METODO_NO_PERMITIDO";
            mensaje = "Método HTTP no permitido para esta ruta";
        } else if (status == 415) {
            codigo = "MEDIA_TYPE_NO_SOPORTADO";
            mensaje = "Content-Type no soportado; usa application/json";
        } else if (status >= 500) {
            codigo = "ERROR_INTERNO";
            mensaje = "Ocurrió un error interno";
        } else {
            codigo = "ERROR_PETICION";
            mensaje = "No se pudo procesar la petición";
        }
        String ruta = request instanceof ServletWebRequest servlet ? servlet.getRequest().getRequestURI() : "";
        return ResponseEntity.status(statusCode).headers(headers)
                .body(cuerpo(status, codigo, mensaje, ruta));
    }

    private ResponseEntity<ErrorDTO> respuesta(HttpStatus status, String codigo, String mensaje, String ruta) {
        return ResponseEntity.status(status).body(cuerpo(status.value(), codigo, mensaje, ruta));
    }

    private ErrorDTO cuerpo(int status, String codigo, String mensaje, String ruta) {
        return new ErrorDTO(LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS), status, codigo, mensaje, ruta);
    }
}
