package com.example.soccermanagement.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Holds configuration properties used by the shared service.
 */
@ConfigurationProperties(prefix = "football-data")
public record FootballDataProperties(
        String baseUrl,
        String apiToken
) {
}
