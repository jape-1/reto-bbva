package com.bbva.reto.config;

//unica exception propia del proyecto, cuando no encuentra recursos
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
