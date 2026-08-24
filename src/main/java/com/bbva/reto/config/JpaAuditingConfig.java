package com.bbva.reto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

//Habilita JPA Auditing para que @CreatedDate y @LastModifiedDate se llenensolas al persistir//

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
