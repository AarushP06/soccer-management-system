package com.example.soccermanagement.match.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateMatchRequest(@NotNull UUID leagueId, @NotNull UUID homeTeamId, @NotNull UUID awayTeamId, @NotNull UUID stadiumId) {}

