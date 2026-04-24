package com.example.soccermanagement.match.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateMatchRequest(@NotBlank String leagueId, @NotBlank String homeTeamId, @NotBlank String awayTeamId, @NotBlank String stadiumId) {}
