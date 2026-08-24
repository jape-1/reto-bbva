package com.bbva.reto.notificacion;

import com.bbva.reto.cliente.event.ClienteCreado;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

//entrada del modulo notificacion: reacciona al alta de cliente, (el aviso es simulado)
@Component
@Slf4j
public class NotificacionListener {

    //AFTER_COMMIT: solo se avisa si el alta del cliente llego a confirmarse
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alCrearse(ClienteCreado evento) {
        log.info("Bienvenida enviada a {}", evento.email());
    }
}
