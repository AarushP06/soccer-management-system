package com.example.soccermanagement.league.infrastructure.integration;

import com.example.soccermanagement.shared.infrastructure.client.FootballDataBaseClient;
import org.springframework.stereotype.Component;

@Component
public class FootballDataLeagueClient {

    private final FootballDataBaseClient client;

    public FootballDataLeagueClient(FootballDataBaseClient client) {
        this.client = client;
    }

    public String getCompetitions() {
        return client.get("/competitions");
    }
}
