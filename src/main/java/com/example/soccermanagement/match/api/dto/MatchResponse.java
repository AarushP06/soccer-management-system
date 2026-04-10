package com.example.soccermanagement.match.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record MatchResponse(
        @Schema(description = "Local match id", example = "44444444-4444-4444-4444-444444444444") UUID id,
        @Schema(description = "External match id from football-data", example = "123456") String externalMatchId,
        @Schema(description = "Local league id", example = "11111111-1111-1111-1111-111111111111") UUID leagueId,
        @Schema(description = "Local league name", example = "Primeira Liga") String leagueName,
        @Schema(description = "External league code (football-data)", example = "PPL") String externalLeagueCode,
        @Schema(description = "Local home team id", example = "22222222-2222-2222-2222-222222222221") UUID homeTeamId,
        @Schema(description = "Local home team name", example = "Benfica") String homeTeamName,
        @Schema(description = "External home team id (football-data)", example = "65") String externalHomeTeamId,
        @Schema(description = "Local away team id", example = "22222222-2222-2222-2222-222222222222") UUID awayTeamId,
        @Schema(description = "Local away team name", example = "Porto") String awayTeamName,
        @Schema(description = "External away team id (football-data)", example = "66") String externalAwayTeamId,
        @Schema(description = "Local stadium id", example = "33333333-3333-3333-3333-333333333333") UUID stadiumId,
        @Schema(description = "Local stadium name", example = "Estádio da Luz") String stadiumName,
        @Schema(description = "External venue id for the stadium", example = "670") Integer externalVenueId,
        @Schema(description = "Match status", example = "SCHEDULED") String status
) {
}
