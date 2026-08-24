package com.bbva.reto.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

//manejo de errores centralizado para toda la api

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    //formato de error para cualqueir caso
    public record RespuestaError(String mensaje, int status, LocalDateTime timestamp) {

        static RespuestaError de(HttpStatus status, String mensaje) {
            return new RespuestaError(mensaje, status.value(), LocalDateTime.now());
        }
    }

    //Falla @Valid en el DTO de entrada: se devuelven todos los campos invalidos.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RespuestaError manejarValidacion(MethodArgumentNotValidException excepcion) {
        String detalle = excepcion.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return RespuestaError.de(HttpStatus.BAD_REQUEST, detalle);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public RespuestaError manejarNoEncontrado(RecursoNoEncontradoException excepcion) {
        return RespuestaError.de(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }

    // Red de seguridad: el detalle va al log
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RespuestaError manejarGenerica(Exception excepcion) {
        log.error("Error no controlado", excepcion);
        return RespuestaError.de(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }
}
