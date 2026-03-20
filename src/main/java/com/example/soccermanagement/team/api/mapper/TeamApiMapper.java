package com.example.soccermanagement.team.api.mapper;

import com.example.soccermanagement.team.api.dto.TeamResponse;
import com.example.soccermanagement.team.domain.Team;

public final class TeamApiMapper {

    private TeamApiMapper() {
    }

    public static TeamResponse toResponse(Team aggregate) {
        return new TeamResponse(aggregate.getId(), aggregate.getName());
    }
}
