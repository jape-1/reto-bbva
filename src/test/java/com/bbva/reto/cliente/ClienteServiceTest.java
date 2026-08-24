package com.bbva.reto.cliente;

import com.bbva.reto.cliente.dto.ClienteRequest;
import com.bbva.reto.cliente.dto.ClienteResponse;
import com.bbva.reto.cliente.event.ClienteCreado;
import com.bbva.reto.cliente.event.ClienteEliminado;
import com.bbva.reto.config.RecursoNoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    //un solo literal: el fixture y el verify no pueden desalinearse
    private static final String EMAIL = "ana.torres@banco.pe";

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ApplicationEventPublisher publicadorDeEventos;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    @DisplayName("crear guarda el cliente y publica ClienteCreado")
    void crearPublicaElEvento() {
        ClienteRequest solicitud = new ClienteRequest(
                "Ana", "Torres", "12345678", EMAIL, "987654321", EstadoCliente.ACTIVO);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteGuardado());

        ClienteResponse respuesta = clienteService.crear(solicitud);

        assertThat(respuesta.id()).isEqualTo(1L);
        assertThat(respuesta.estado()).isEqualTo(EstadoCliente.ACTIVO);
        //el modulo cliente no llama a auditoria ni a notificacion, solo publica
        verify(publicadorDeEventos).publishEvent(new ClienteCreado(1L, EMAIL));
    }

    @Test
    @DisplayName("obtenerPorId lanza RecursoNoEncontradoException si el id no existe")
    void obtenerPorIdInexistenteFalla() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
        verifyNoInteractions(publicadorDeEventos);
    }

    @Test
    @DisplayName("eliminar da de baja al cliente sin borrar la fila")
    void eliminarEsBajaLogica() {
        Cliente existente = clienteGuardado();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));

        clienteService.eliminar(1L);

        assertThat(existente.getEstado()).isEqualTo(EstadoCliente.ELIMINADO);
        verify(clienteRepository, never()).delete(any(Cliente.class));
        verify(clienteRepository).save(existente);
        verify(publicadorDeEventos).publishEvent(new ClienteEliminado(1L));
    }

    private Cliente clienteGuardado() {
        return Cliente.builder()
                .id(1L)
                .nombres("Ana")
                .apellidos("Torres")
                .numeroDocumento("12345678")
                .email(EMAIL)
                .telefono("987654321")
                .estado(EstadoCliente.ACTIVO)
                .build();
    }
}
