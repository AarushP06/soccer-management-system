package com.example.soccermanagement.team.infrastructure.integration;

import com.example.soccermanagement.shared.exception.ExternalApiRateLimitException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;

@Component
public class FootballDataLeagueClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;
    private final String apiToken;

    public FootballDataLeagueClient(@Value("${external.football-data.url:https://api.football-data.org/v2}") String baseUrl,
                                    @Value("${football-data.api-token:${FOOTBALL_DATA_API_TOKEN:}}") String apiToken) {
        this.baseUrl = baseUrl;
        this.apiToken = apiToken;
    }

    public record Team(String id, String name) {}

    public record CompetitionDto(String name) {}

    public java.util.List<Team> getTeamsByCompetitionCode(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (apiToken != null && !apiToken.isBlank()) {
                headers.set("X-Auth-Token", apiToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            var arr = restTemplate.exchange(baseUrl + "/competitions/{code}/teams", org.springframework.http.HttpMethod.GET, entity, Team[].class, code).getBody();
            if (arr == null) return java.util.Collections.emptyList();
            return java.util.Arrays.asList(arr);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return java.util.Collections.emptyList();
            }
            throw ex;
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new ExternalApiRateLimitException("External API rate limit exceeded", ex);
            }
            throw ex;
        }
    }

    public CompetitionDto getCompetitionByCode(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (apiToken != null && !apiToken.isBlank()) {
                headers.set("X-Auth-Token", apiToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(baseUrl + "/competitions/{code}", org.springframework.http.HttpMethod.GET, entity, CompetitionDto.class, code).getBody();
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw ex;
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new ExternalApiRateLimitException("External API rate limit exceeded", ex);
            }
            throw ex;
        }
    }
}
