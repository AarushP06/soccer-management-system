package com.example.soccermanagement.match.infrastructure.integration;

import com.example.soccermanagement.shared.infrastructure.client.FootballDataBaseClient;
import org.springframework.stereotype.Component;

@Component
public class FootballDataMatchClient {

    private final FootballDataBaseClient client;

    public FootballDataMatchClient(FootballDataBaseClient client) {
        this.client = client;
    }

    public String getMatchesByCompetitionCode(String code) {
        return client.get("/competitions/" + code + "/matches");
    }
}
