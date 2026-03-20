package com.example.soccermanagement.match.api.mapper;

import com.example.soccermanagement.match.api.dto.MatchResponse;
import com.example.soccermanagement.match.domain.Match;

public final class MatchApiMapper {

    private MatchApiMapper() {
    }

    public static MatchResponse toResponse(Match match) {
        return new MatchResponse(
                match.getId(),
                match.getLeagueId(),
                match.getHomeTeamId(),
                match.getAwayTeamId(),
                match.getStadiumId(),
                match.getStatus()
        );
    }
}
