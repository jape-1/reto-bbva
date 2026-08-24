package com.bbva.reto.cliente.event;

//evento publicado tras dar de alta un cliente
public record ClienteCreado(Long clienteId, String email) {
}
