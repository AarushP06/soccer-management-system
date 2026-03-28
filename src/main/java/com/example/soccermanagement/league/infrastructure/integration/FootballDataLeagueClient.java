package com.example.soccermanagement.league.infrastructure.integration;

import com.example.soccermanagement.shared.infrastructure.client.FootballDataBaseClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class FootballDataLeagueClient {

    private final FootballDataBaseClient client;

    public FootballDataLeagueClient(FootballDataBaseClient client) {
        this.client = client;
    }

    public Optional<Competition> getCompetitionByCode(String code) {
        CompetitionsResponse response = client.get("/competitions", CompetitionsResponse.class);
        if (response == null || response.competitions() == null) {
            return Optional.empty();
        }
        return response.competitions().stream()
                .filter(c -> c.code() != null && c.code().equalsIgnoreCase(code.trim()))
                .findFirst();
    }

    public List<Team> getTeamsByCompetitionCode(String code) {
        if (code == null || code.isBlank()) {
            return List.of();
        }
        String normalizedCode = code.trim();
        TeamsResponse response = client.get("/competitions/{code}/teams", TeamsResponse.class, normalizedCode);
        if (response == null || response.teams() == null) {
            return List.of();
        }
        return response.teams().stream()
                .filter(t -> t != null && t.name() != null && !t.name().isBlank())
                .toList();
    }

    public record CompetitionsResponse(List<Competition> competitions) {
    }

    public record Competition(String code, String name) {
    }

    public record TeamsResponse(List<Team> teams) {
    }

    public record Team(String name) {
    }
}
