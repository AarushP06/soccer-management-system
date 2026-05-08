package com.example.soccermanagement.stadium;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Exposes a lightweight health endpoint for this service.
 */
@RestController
@RequestMapping("/ping")
public class PingController {

    @GetMapping
    public Map<String, String> ping() {
        return Map.of(
                "service", "stadium-service",
                "status", "up"
        );
    }
}
