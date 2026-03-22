package com.example.soccermanagement.shared.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "api-football")
public record ApiFootballProperties(
        String baseUrl,
        String apiKey
) {
}