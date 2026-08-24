-- Estado de baja, separado de INACTIVO (que es un estado de negocio).
-- Va en una migracion nueva y no editando V1: una migracion ya aplicada no se toca.

ALTER TABLE cliente DROP CONSTRAINT ck_cliente_estado;

ALTER TABLE cliente ADD CONSTRAINT ck_cliente_estado
    CHECK (estado IN ('ACTIVO', 'INACTIVO', 'BLOQUEADO', 'ELIMINADO'));
