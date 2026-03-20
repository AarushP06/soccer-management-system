package com.example.soccermanagement.league.api.mapper;

import com.example.soccermanagement.league.api.dto.LeagueResponse;
import com.example.soccermanagement.league.domain.League;

public final class LeagueApiMapper {

    private LeagueApiMapper() {
    }

    public static LeagueResponse toResponse(League aggregate) {
        return new LeagueResponse(aggregate.getId(), aggregate.getName());
    }
}
