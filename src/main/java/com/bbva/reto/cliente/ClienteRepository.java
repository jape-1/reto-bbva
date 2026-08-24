package com.bbva.reto.cliente;

import org.springframework.data.jpa.repository.JpaRepository;

//repositorio de lciente
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
