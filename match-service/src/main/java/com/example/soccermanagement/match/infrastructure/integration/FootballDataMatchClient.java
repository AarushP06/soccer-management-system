package com.example.soccermanagement.match.infrastructure.integration;

import com.example.soccermanagement.shared.exception.ExternalApiRateLimitException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Component
public class FootballDataMatchClient {
    private final WebClient webClient;

    public FootballDataMatchClient(@Value("${external.football-data.url:https://api.football-data.org/v2}") String baseUrl,
                                   @Value("${football-data.api-token:${FOOTBALL_DATA_API_TOKEN:}}") String apiToken) {
        WebClient.Builder builder = WebClient.builder().baseUrl(baseUrl);
        if (apiToken != null && !apiToken.isBlank()) {
            builder.defaultHeader("X-Auth-Token", apiToken);
        }
        this.webClient = builder.build();
    }

    public record ExternalMatch(String id, Team homeTeam, Team awayTeam) {}
    public record Team(String id, String name) {}

    public java.util.List<ExternalMatch> getMatchesByCompetitionCode(String code) {
        try {
            var resp = webClient.get().uri(uri -> uri.path("/competitions/{code}/matches").build(code)).retrieve().bodyToMono(MatchesResponse.class).block();
            if (resp == null || resp.matches() == null) return List.of();
            return resp.matches();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) return List.of();
            if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new ExternalApiRateLimitException("External API rate limit exceeded", ex);
            }
            throw ex;
        }
    }

    public record MatchesResponse(java.util.List<ExternalMatch> matches) {}

    public record CompetitionDto(String name) {}

    public CompetitionDto getCompetitionByCode(String code) {
        try {
            return webClient.get().uri(uri -> uri.path("/competitions/{code}").build(code)).retrieve().bodyToMono(CompetitionDto.class).block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) return null;
            if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new ExternalApiRateLimitException("External API rate limit exceeded", ex);
            }
            throw ex;
        }
    }
}
