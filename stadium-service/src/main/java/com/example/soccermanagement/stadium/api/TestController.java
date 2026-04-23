package com.example.soccermanagement.stadium.api;

import com.example.soccermanagement.shared.exception.ExternalApiRateLimitException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping("/rate-limit")
    public void simulateRateLimit() {
        throw new ExternalApiRateLimitException("Simulated rate limit");
    }
}

