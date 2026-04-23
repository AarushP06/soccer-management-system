package com.example.soccermanagement.gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/ping")
public class PingController {

    @GetMapping
    public Mono<Map<String, String>> ping() {
        return Mono.just(Map.of(
                "service", "api-gateway",
                "status", "up"
        ));
    }
}
