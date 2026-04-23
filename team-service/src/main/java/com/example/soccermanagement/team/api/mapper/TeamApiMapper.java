package com.example.soccermanagement.team.api.mapper;

import com.example.soccermanagement.team.api.dto.TeamResponse;
import com.example.soccermanagement.team.domain.Team;

public final class TeamApiMapper {
    public static TeamResponse toResponse(Team team) {
        return new TeamResponse(team.getId(), team.getName(), team.getExternalId());
    }
}

