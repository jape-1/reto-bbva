package com.bbva.reto.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("API de Gestion de Clientes Bancarios")
                .version("v1")
                .description("Reto tecnico BBVA. Monolito modular: el modulo cliente publica "
                        + "eventos de dominio que consumen auditoria y notificacion."));
    }
}
