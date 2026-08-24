package com.bbva.reto.cliente.event;

//evento publicado tras eliminar un cliente, solo lo escucha auditoria
public record ClienteEliminado(Long clienteId) {
}
