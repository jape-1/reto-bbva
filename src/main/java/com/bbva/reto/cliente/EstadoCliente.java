package com.bbva.reto.cliente;

public enum EstadoCliente {
    ACTIVO,
    INACTIVO,
    BLOQUEADO,
    //estado de baja, solo se alcanza via DELETE
    ELIMINADO
}
