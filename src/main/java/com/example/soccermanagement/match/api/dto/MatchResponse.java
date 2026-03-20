package com.example.soccermanagement.match.api.dto;

import java.util.UUID;

public record MatchResponse(
        UUID id,
        UUID leagueId,
        UUID homeTeamId,
        UUID awayTeamId,
        UUID stadiumId,
        String status
) {
}
