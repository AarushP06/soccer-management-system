package com.example.soccermanagement.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
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
                        .description("Local-only development API for leagues, teams, stadiums and matches.\n\nThis deployment uses local JSON imports (POST /api/.../import/local) and H2 for development. External 3rd-party APIs are not required or documented here."))
                .info(new Info().contact(new Contact().name("Soccer Management Team").email("dev-team@example.com")));
    }
}
