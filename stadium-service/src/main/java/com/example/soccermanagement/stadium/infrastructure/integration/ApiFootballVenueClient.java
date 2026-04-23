package com.example.soccermanagement.stadium.infrastructure.integration;

import com.example.soccermanagement.shared.exception.ExternalApiRateLimitException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Component
public class ApiFootballVenueClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    public ApiFootballVenueClient(@Value("${api-football.base-url:https://api-football.example.com}") String baseUrl,
                                  @Value("${api-football.api-key:${API_FOOTBALL_API_KEY:}}") String apiKey) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public Optional<ExternalVenue> getVenueById(Integer venueId) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path("/venues")
                    .queryParam("id", venueId)
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ACCEPT, "application/json");
            if (apiKey != null && !apiKey.isBlank()) {
                headers.set("x-apisports-key", apiKey);
            }

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<VenueResponse> resp = restTemplate.exchange(uri, HttpMethod.GET, entity, VenueResponse.class);
            if (resp != null && resp.getBody() != null && resp.getBody().response() != null && !resp.getBody().response().isEmpty()) {
                return Optional.of(resp.getBody().response().get(0));
            }
            return Optional.empty();
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().value() == 404) return Optional.empty();
            if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new ExternalApiRateLimitException("External API rate limit exceeded", ex);
            }
            throw ex;
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == org.springframework.http.HttpStatus.TOO_MANY_REQUESTS) {
                throw new ExternalApiRateLimitException("External API rate limit exceeded", ex);
            }
            throw ex;
        } catch (RestClientResponseException ex) {
            // propagate other client errors
            throw ex;
        }
    }

    public TeamsResponse getTeamsByLeagueAndSeason(Integer leagueId, Integer season) {
        if (leagueId == null || season == null) return new TeamsResponse(List.of());
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path("/teams")
                    .queryParam("league", leagueId)
                    .queryParam("season", season)
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ACCEPT, "application/json");
            if (apiKey != null && !apiKey.isBlank()) {
                headers.set("x-apisports-key", apiKey);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<TeamsResponse> resp = restTemplate.exchange(uri, HttpMethod.GET, entity, TeamsResponse.class);
            if (resp == null || resp.getBody() == null || resp.getBody().response() == null) return new TeamsResponse(List.of());
            return resp.getBody();
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().value() == 429) {
                throw new ExternalApiRateLimitException("External API rate limit exceeded", ex);
            }
            return new TeamsResponse(List.of());
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == org.springframework.http.HttpStatus.TOO_MANY_REQUESTS) {
                throw new ExternalApiRateLimitException("External API rate limit exceeded", ex);
            }
            return new TeamsResponse(List.of());
        } catch (RestClientResponseException ex) {
            return new TeamsResponse(List.of());
        }
    }

    public record VenueResponse(java.util.List<ExternalVenue> response) {}
    public record TeamsResponse(java.util.List<Team> response) {}
    public record ExternalVenue(Integer id, String name, String city, String country, Integer capacity) {}
    public record Team(Integer id, String name, ExternalVenue venue) {}
}
