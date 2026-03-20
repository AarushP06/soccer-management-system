package com.example.soccermanagement.team.infrastructure.integration;

import com.example.soccermanagement.shared.infrastructure.client.FootballDataBaseClient;
import org.springframework.stereotype.Component;

@Component
public class FootballDataTeamClient {

    private final FootballDataBaseClient client;

    public FootballDataTeamClient(FootballDataBaseClient client) {
        this.client = client;
    }

    public String getTeamsByCompetitionCode(String code) {
        return client.get("/competitions/" + code + "/teams");
    }
}
