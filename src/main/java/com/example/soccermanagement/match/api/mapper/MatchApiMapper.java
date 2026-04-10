package com.example.soccermanagement.match.api.mapper;

import com.example.soccermanagement.match.api.dto.MatchResponse;
import com.example.soccermanagement.match.domain.Match;

public final class MatchApiMapper {

    private MatchApiMapper() {
    }

    public static MatchResponse toResponse(Match match) {
        // backward-compatible mapping when names/external ids are not available
        return new MatchResponse(
                match.getId(),
                null,
                match.getLeagueId(),
                null,
                null,
                match.getHomeTeamId(),
                null,
                null,
                match.getAwayTeamId(),
                null,
                null,
                match.getStadiumId(),
                null,
                null,
                match.getStatus()
        );
    }

    public static MatchResponse toResponse(Match match,
                                           String externalMatchId,
                                           String leagueName,
                                           String externalLeagueCode,
                                           String homeTeamName,
                                           String externalHomeTeamId,
                                           String awayTeamName,
                                           String externalAwayTeamId,
                                           String stadiumName,
                                           Integer externalVenueId) {
        return new MatchResponse(
                match.getId(),
                externalMatchId,
                match.getLeagueId(),
                leagueName,
                externalLeagueCode,
                match.getHomeTeamId(),
                homeTeamName,
                externalHomeTeamId,
                match.getAwayTeamId(),
                awayTeamName,
                externalAwayTeamId,
                match.getStadiumId(),
                stadiumName,
                externalVenueId,
                match.getStatus()
        );
    }
}
