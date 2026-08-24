package com.bbva.reto.cliente;

import com.bbva.reto.cliente.dto.ClienteRequest;
import com.bbva.reto.cliente.dto.ClienteResponse;
import com.bbva.reto.cliente.event.ClienteActualizado;
import com.bbva.reto.cliente.event.ClienteCreado;
import com.bbva.reto.cliente.event.ClienteEliminado;
import com.bbva.reto.config.RecursoNoEncontradoException;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


//Logica de negocio del modulo cliente

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ApplicationEventPublisher publicadorDeEventos;

    @Transactional
    public ClienteResponse crear(ClienteRequest solicitud) {
        Cliente cliente = Cliente.builder()
                .nombres(solicitud.nombres())
                .apellidos(solicitud.apellidos())
                .numeroDocumento(solicitud.numeroDocumento())
                .email(solicitud.email())
                .telefono(solicitud.telefono())
                .estado(solicitud.estado())
                .build();
        // fechaCreacion y fechaActualizacion las escribe JPA Auditing
        Cliente guardado = clienteRepository.save(cliente);
        publicadorDeEventos.publishEvent(new ClienteCreado(guardado.getId(), guardado.getEmail()));
        return ClienteResponse.desde(guardado);
    }

    public List<ClienteResponse> listar() {
        return clienteRepository.findAll()
                .stream()
                .map(ClienteResponse::desde)
                .toList();
    }

    public ClienteResponse obtenerPorId(Long id) {
        return ClienteResponse.desde(buscarOFallar(id));
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest solicitud) {
        Cliente cliente = buscarOFallar(id);
        cliente.setNombres(solicitud.nombres());
        cliente.setApellidos(solicitud.apellidos());
        cliente.setNumeroDocumento(solicitud.numeroDocumento());
        cliente.setEmail(solicitud.email());
        cliente.setTelefono(solicitud.telefono());
        cliente.setEstado(solicitud.estado());
        // El save es explicito por legibilidad; dentro de la transaccion el
        // dirty checking de JPA ya persistiria los cambios igual.
        Cliente actualizado = clienteRepository.save(cliente);
        publicadorDeEventos.publishEvent(new ClienteActualizado(actualizado.getId()));
        return ClienteResponse.desde(actualizado);
    }

    //el cliente pasa a ELIMINADO
    @Transactional
    public void eliminar(Long id) {
        Cliente cliente = buscarOFallar(id);
        //DELETE es idempotente: si ya esta de baja no se repite la traza de auditoria
        if (cliente.getEstado() == EstadoCliente.ELIMINADO) {
            return;
        }
        cliente.setEstado(EstadoCliente.ELIMINADO);
        clienteRepository.save(cliente);
        publicadorDeEventos.publishEvent(new ClienteEliminado(id));
    }

    private Cliente buscarOFallar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el cliente con id " + id));
    }
}
