package com.example.soccermanagement.match.application.dto;

import java.util.UUID;

/**
 * Carries structured data used by the match service.
 */
public record LeagueInfo(UUID id, String name, String externalCode) {
    // Backwards-compatible JavaBean-style getters used by existing code
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getExternalCode() { return externalCode; }
}
