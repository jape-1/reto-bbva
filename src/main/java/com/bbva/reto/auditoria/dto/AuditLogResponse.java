package com.bbva.reto.auditoria.dto;

import com.bbva.reto.auditoria.AccionAuditoria;
import com.bbva.reto.auditoria.AuditLog;

import java.time.LocalDateTime;

//dto de salida de auditoria, la entidad no sale por la api
public record AuditLogResponse(
        Long id,
        String entidad,
        Long entidadId,
        AccionAuditoria accion,
        LocalDateTime fecha
) {

    public static AuditLogResponse desde(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getEntidad(),
                auditLog.getEntidadId(),
                auditLog.getAccion(),
                auditLog.getFecha()
        );
    }
}
