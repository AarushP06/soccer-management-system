package com.example.soccermanagement.league.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateLeagueRequest(@Schema(description = "Name of the league", example = "Premier League") @NotBlank String name) {
}
