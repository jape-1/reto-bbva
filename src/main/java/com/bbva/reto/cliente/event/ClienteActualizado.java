package com.bbva.reto.cliente.event;

//evento publicado tras actualizar un cliente, solo lo escucha auditoria
public record ClienteActualizado(Long clienteId) {
}
