package com.bbva.reto.auditoria;

import com.bbva.reto.auditoria.dto.AuditLogResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//solo lectura de auditoria, para comprobar que el evento persistio
@RestController
@RequestMapping("/auditoria")
@Tag(name = "Auditoria", description = "Consulta de la traza generada por los eventos de dominio")
@AllArgsConstructor
public class AuditoriaController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping("/{clienteId}")
    public List<AuditLogResponse> obtenerPorCliente(@PathVariable Long clienteId) {
        return auditLogRepository.findByEntidadIdOrderByFechaAsc(clienteId)
                .stream()
                .map(AuditLogResponse::desde)
                .toList();
    }
}
