package com.example.soccermanagement.league.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents incoming API data for a league request.
 */
public record CreateLeagueRequest(@Schema(description = "Name of the league", example = "Premier League") @NotBlank String name) {
}
