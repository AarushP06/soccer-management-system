package com.example.soccermanagement.team.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents incoming API data for a team request.
 */
public record UpdateTeamRequest(@NotBlank String name) {
}
