package com.bbva.reto.cliente.dto;

import com.bbva.reto.cliente.Cliente;
import com.bbva.reto.cliente.EstadoCliente;

import java.time.LocalDateTime;

//dto de salida de cliente
public record ClienteResponse(
        Long id,
        String nombres,
        String apellidos,
        String numeroDocumento,
        String email,
        String telefono,
        EstadoCliente estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {

    // Mapeo desde la entidad; lo invoca el service
    public static ClienteResponse desde(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNombres(),
                cliente.getApellidos(),
                cliente.getNumeroDocumento(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getEstado(),
                cliente.getFechaCreacion(),
                cliente.getFechaActualizacion()
        );
    }
}
