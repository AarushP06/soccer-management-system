package com.example.soccermanagement.match.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateMatchRequest(
        @Schema(description = "Local league id", example = "11111111-1111-1111-1111-111111111111") @NotNull UUID leagueId,
        @Schema(description = "Local home team id", example = "22222222-2222-2222-2222-222222222221") @NotNull UUID homeTeamId,
        @Schema(description = "Local away team id", example = "22222222-2222-2222-2222-222222222222") @NotNull UUID awayTeamId,
        @Schema(description = "Local stadium id to host the match", example = "33333333-3333-3333-3333-333333333333") @NotNull UUID stadiumId
) {
}
