package com.example.soccermanagement.match.infrastructure.integration;
import com.example.soccermanagement.shared.infrastructure.client.FootballDataBaseClient;
import org.springframework.stereotype.Component;
import java.util.List;
@Component
public class FootballDataMatchClient {
    private final FootballDataBaseClient client;
    public FootballDataMatchClient(FootballDataBaseClient client) {
        this.client = client;
    }
    public List<ExternalMatch> getMatchesByCompetitionCode(String code) {
        MatchesResponse response = client.get("/competitions/{code}/matches", MatchesResponse.class, code);
        return response.matches();
    }
    public record MatchesResponse(List<ExternalMatch> matches) {
    }
    public record ExternalMatch(
            Team homeTeam,
            Team awayTeam
    ) {
    }
    public record Team(String name) {
    }
}