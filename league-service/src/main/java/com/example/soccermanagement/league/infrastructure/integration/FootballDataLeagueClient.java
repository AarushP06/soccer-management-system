package com.example.soccermanagement.league.infrastructure.integration;

import com.example.soccermanagement.league.domain.exception.LeagueNotFoundException;
import com.example.soccermanagement.shared.exception.ExternalApiRateLimitException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;

@Component
public class FootballDataLeagueClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;
    private final String apiToken;
    private final ObjectMapper mapper = new ObjectMapper();

    public FootballDataLeagueClient(@Value("${external.football-data.url:https://api.football-data.org/v2}") String baseUrl,
                                    @Value("${football-data.api-token:${FOOTBALL_DATA_API_TOKEN:}}") String apiToken) {
        this.baseUrl = baseUrl;
        this.apiToken = apiToken;
    }

    private <T> T loadSeed(String resourcePath, Class<T> type) {
        try {
            ClassPathResource r = new ClassPathResource(resourcePath);
            if (!r.exists()) return null;
            try (InputStream is = r.getInputStream()) {
                return mapper.readValue(is, type);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load seed resource: " + resourcePath, e);
        }
    }

    public CompetitionDto getCompetitionByCode(String code) {
        // fallback to seed when no api token configured
        if (apiToken == null || apiToken.isBlank()) {
            String resource = "data/competitions-" + (code == null ? "" : code) + ".json";
            CompetitionDto seed = loadSeed(resource, CompetitionDto.class);
            if (seed != null) return seed;
            throw new LeagueNotFoundException("External competition not found for code: " + code);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            if (apiToken != null && !apiToken.isBlank()) {
                headers.set("X-Auth-Token", apiToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(baseUrl + "/competitions/{code}", org.springframework.http.HttpMethod.GET, entity, CompetitionDto.class, code).getBody();
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new LeagueNotFoundException("External competition not found for code: " + code);
            }
            throw ex;
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new ExternalApiRateLimitException("External API rate limit exceeded", ex);
            }
            throw ex;
        }
    }

    public record CompetitionDto(String name) {}
}
