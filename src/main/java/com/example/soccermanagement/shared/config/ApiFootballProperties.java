package com.example.soccermanagement.shared.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * Holds configuration properties used by the shared service.
 */
@ConfigurationProperties(prefix = "api-football")
public record ApiFootballProperties(
        String baseUrl,
        String apiKey
) {
}
