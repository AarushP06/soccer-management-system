package com.example.soccermanagement.match.infrastructure.integration;

import com.example.soccermanagement.shared.exception.ExternalApiRateLimitException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.InputStream;
import java.util.List;

@Component
public class FootballDataMatchClient {
    private final WebClient webClient;
    private final String apiToken;

    public FootballDataMatchClient(@Value("${external.football-data.url:https://api.football-data.org/v2}") String baseUrl,
                                   @Value("${football-data.api-token:${FOOTBALL_DATA_API_TOKEN:}}") String apiToken) {
        WebClient.Builder builder = WebClient.builder().baseUrl(baseUrl);
        if (apiToken != null && !apiToken.isBlank()) {
            builder.defaultHeader("X-Auth-Token", apiToken);
        }
        this.webClient = builder.build();
        this.apiToken = apiToken;
    }

    public record ExternalMatch(String id, Team homeTeam, Team awayTeam) {}
    public record Team(String id, String name) {}

    public java.util.List<ExternalMatch> getMatchesByCompetitionCode(String code) {
        // fallback to seeds if no api token configured
        if (apiToken == null || apiToken.isBlank()) {
            String resource = "seeds/matches-" + (code == null ? "" : code) + ".json";
            try {
                ClassPathResource r = new ClassPathResource(resource);
                if (!r.exists()) return List.of();
                try (InputStream is = r.getInputStream()) {
                    ObjectMapper mapper = new ObjectMapper();
                    MatchesResponse seed = mapper.readValue(is, MatchesResponse.class);
                    if (seed == null || seed.matches() == null) return List.of();
                    return seed.matches();
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to load seed resource: " + resource, e);
            }
        }
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
        if (apiToken == null || apiToken.isBlank()) {
            String resource = "seeds/competitions-" + (code == null ? "" : code) + ".json";
            try {
                ClassPathResource r = new ClassPathResource(resource);
                if (!r.exists()) return null;
                try (InputStream is = r.getInputStream()) {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(is, CompetitionDto.class);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to load seed resource: " + resource, e);
            }
        }
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
