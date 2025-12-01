package com.fintech.banking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("FinTech Mobile Banking API")
                        .version(appVersion)
                        .description("Secure RESTful API for FinTech Mobile Banking Application. " +
                                "Provides endpoints for account management, transactions, cards, " +
                                "beneficiaries, loans, and investments.")
                        .contact(new Contact()
                                .name("API Support Team")
                                .email("api-support@fintech.com")
                                .url("https://fintech.com/support"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://fintech.com/terms")))
                .servers(List.of(
                        new Server().url("/api/v1").description("API v1")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Authentication token. Obtain via /auth/login endpoint.")));
    }
}
