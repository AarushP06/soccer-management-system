package com.example.soccermanagement.league.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents incoming API data for a league request.
 */
public record LeagueRequest(
        @NotBlank(message = "name must not be blank") String name
) {
}

