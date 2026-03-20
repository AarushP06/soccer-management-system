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
        return response.competitions().stream()
                .filter(c -> c.code().equals(code))
                .findFirst();
    }

    public List<Team> getTeamsByCompetitionCode(String code) {
        TeamsResponse response = client.get("/competitions/{code}/teams", TeamsResponse.class, code);
        return response.teams();
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
