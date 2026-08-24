package com.bbva.reto.auditoria;

import com.bbva.reto.cliente.event.ClienteActualizado;
import com.bbva.reto.cliente.event.ClienteCreado;
import com.bbva.reto.cliente.event.ClienteEliminado;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

//entrada del modulo auditoria reacciona a eventos de cliente
@Component
@AllArgsConstructor
public class AuditoriaListener {

    private static final String ENTIDAD_CLIENTE = "Cliente";

    private final AuditLogRepository auditLogRepository;

    //AFTER_COMMIT esla traza se escribe solo si la transaccion del cliente confirmo
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alCrearse(ClienteCreado evento) {
        registrar(evento.clienteId(), AccionAuditoria.CREADO);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alActualizarse(ClienteActualizado evento) {
        registrar(evento.clienteId(), AccionAuditoria.ACTUALIZADO);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alEliminarse(ClienteEliminado evento) {
        registrar(evento.clienteId(), AccionAuditoria.ELIMINADO);
    }

    private void registrar(Long clienteId, AccionAuditoria accion) {
        auditLogRepository.save(AuditLog.builder()
                .entidad(ENTIDAD_CLIENTE)
                .entidadId(clienteId)
                .accion(accion)
                .build());
    }
}
