package com.example.soccermanagement.match;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/ping")
public class PingController {

    @GetMapping
    public Map<String, String> ping() {
        return Map.of(
                "service", "match-service",
                "status", "up"
        );
    }
}
