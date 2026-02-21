package com.example.modern_api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Vanguard Wallet System API", version = "v1.0.0", description = "A production-ready robust Wallet System demonstrating idempotency, concurrency control, and double-entry bookkeeping.", contact = @Contact(name = "Antigravity Engineer", email = "engineer@example.com"), license = @License(name = "Apache 2.0", url = "http://springdoc.org")))
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("vanguard-public-api")
                .pathsToMatch("/api/**")
                .build();
    }
}
