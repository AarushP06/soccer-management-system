package com.example.soccermanagement.match.api.dto;

import java.util.UUID;

/**
 * Represents outgoing API data for the match service.
 */
public record MatchResponse(
        UUID id,
        UUID leagueId,
        UUID homeTeamId,
        UUID awayTeamId,
        UUID stadiumId,
        String status,
        String externalMatchId,
        String leagueName,
        String homeTeamName,
        String awayTeamName,
        String stadiumName
) {}
