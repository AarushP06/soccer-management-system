package com.example.soccermanagement.shared.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configures framework or integration components for the shared service.
 */
@Configuration
@EnableConfigurationProperties(FootballDataProperties.class)
public class FootballDataConfig {
}
