package com.example.soccermanagement.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "football-data")
public record FootballDataProperties(
        String baseUrl,
        String apiToken
) {
}
