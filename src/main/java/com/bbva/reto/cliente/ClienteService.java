package com.bbva.reto.cliente;

import com.bbva.reto.cliente.dto.ClienteRequest;
import com.bbva.reto.cliente.dto.ClienteResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;


//Logica de negocio del modulo cliente

@Service
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

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
        return ClienteResponse.desde(clienteRepository.save(cliente));
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
        return ClienteResponse.desde(clienteRepository.save(cliente));
    }

    @Transactional
    public void eliminar(Long id) {
        clienteRepository.delete(buscarOFallar(id));
    }

    private Cliente buscarOFallar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el cliente con id " + id));
    }
}
