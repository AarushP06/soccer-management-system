package com.example.soccermanagement.match.application.dto;

import java.util.UUID;

public record TeamInfo(UUID id, String name, String externalId) {
    // Backwards-compatible getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getExternalId() { return externalId; }
}
