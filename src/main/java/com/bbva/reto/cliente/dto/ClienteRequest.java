package com.bbva.reto.cliente.dto;

import com.bbva.reto.cliente.EstadoCliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

//dto de entrada de cliente con validaciones
public record ClienteRequest(

        @NotBlank(message = "los nombres son obligatorios")
        @Size(max = 100, message = "los nombres no pueden superar 100 caracteres")
        String nombres,

        @NotBlank(message = "los apellidos son obligatorios")
        @Size(max = 100, message = "los apellidos no pueden superar 100 caracteres")
        String apellidos,

        @NotBlank(message = "el numero de documento es obligatorio")
        @Pattern(regexp = "[0-9]{8,20}", message = "el numero de documento debe tener entre 8 y 20 digitos")
        String numeroDocumento,

        @NotBlank(message = "el email es obligatorio")
        @Email(message = "el email no tiene un formato valido")
        @Size(max = 150, message = "el email no puede superar 150 caracteres")
        String email,

        @Pattern(regexp = "[0-9]{7,20}", message = "el telefono debe tener entre 7 y 20 digitos")
        String telefono,

        @NotNull(message = "el estado es obligatorio")
        EstadoCliente estado
) {
}
