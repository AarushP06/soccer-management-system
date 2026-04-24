package com.example.soccermanagement.match.api.mapper;

import com.example.soccermanagement.match.api.dto.MatchResponse;
import com.example.soccermanagement.match.domain.Match;

public final class MatchApiMapper {
    public static MatchResponse toResponse(Match match, String externalMatchId, String leagueName, String externalLeagueCode, String homeName, String externalHomeId, String awayName, String externalAwayId, String stadiumName, Integer externalVenueId) {
        return new MatchResponse(
                match.getId(),
                match.getLeagueId(),
                match.getHomeTeamId(),
                match.getAwayTeamId(),
                match.getStadiumId(),
                match.getStatus(),
                externalMatchId,
                leagueName,
                homeName,
                awayName,
                stadiumName
        );
    }
}
