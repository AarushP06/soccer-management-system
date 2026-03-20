package com.example.soccermanagement.shared.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI soccerManagementApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Soccer Management System API")
                        .version("1.0.0")
                        .description("Modular monolith for leagues, teams, locations, and matches"))
                .externalDocs(new ExternalDocumentation()
                        .description("football-data.org")
                        .url("https://www.football-data.org/documentation/api"));
    }
}
