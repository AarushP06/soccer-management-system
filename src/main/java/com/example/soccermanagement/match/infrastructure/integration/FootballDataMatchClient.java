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
        if (code == null || code.isBlank()) {
            return List.of();
        }
        String normalizedCode = code.trim();
        MatchesResponse response = client.get("/competitions/{code}/matches", MatchesResponse.class, normalizedCode);
        if (response == null || response.matches() == null) {
            return List.of();
        }
        return response.matches().stream()
                .filter(m -> m != null
                        && m.homeTeam() != null && m.homeTeam().name() != null && !m.homeTeam().name().isBlank()
                        && m.awayTeam() != null && m.awayTeam().name() != null && !m.awayTeam().name().isBlank())
                .toList();
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