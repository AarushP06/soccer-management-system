package com.example.soccermanagement.shared.infrastructure.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

public final class SeedResourceLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SeedResourceLoader() {}

    public static <T> T load(String resourcePath, Class<T> type) {
        try {
            ClassPathResource r = new ClassPathResource(resourcePath);
            if (!r.exists()) return null;
            try (InputStream is = r.getInputStream()) {
                return MAPPER.readValue(is, type);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load seed resource: " + resourcePath, e);
        }
    }
}

