package com.example.soccermanagement.team.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents incoming API data for a team request.
 */
public record CreateTeamRequest(@Schema(description = "Name of the team", example = "Benfica") @NotBlank String name) {
}
