package com.example.soccermanagement.stadium.infrastructure.integration;

import com.example.soccermanagement.shared.exception.ExternalApiRateLimitException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
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

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@Component
public class ApiFootballVenueClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;
    private final ObjectMapper mapper = new ObjectMapper();

    public ApiFootballVenueClient(@Value("${api-football.base-url:https://api-football.example.com}") String baseUrl,
                                  @Value("${api-football.api-key:${API_FOOTBALL_API_KEY:}}") String apiKey) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
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

    public Optional<ExternalVenue> getVenueById(Integer venueId) {
        // If no API key configured, fall back to local seeds
        if (apiKey == null || apiKey.isBlank()) {
            String resource = "data/venues.json";
            VenueResponse seed = loadSeed(resource, VenueResponse.class);
            if (seed != null && seed.response() != null && !seed.response().isEmpty()) {
                return seed.response().stream().filter(v -> v.id().equals(venueId)).findFirst();
            }
            return Optional.empty();
        }

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
            if (resp.getBody() != null && resp.getBody().response() != null && !resp.getBody().response().isEmpty()) {
                return Optional.of(resp.getBody().response().get(0));
            }
            return Optional.empty();
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().value() == 404) return Optional.empty();
            if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new ExternalApiRateLimitException("External API rate limit exceeded", ex);
            }
            return Optional.empty();
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == org.springframework.http.HttpStatus.TOO_MANY_REQUESTS) {
                throw new ExternalApiRateLimitException("External API rate limit exceeded", ex);
            }
            return Optional.empty();
        } catch (RestClientResponseException ex) {
            // propagate other client errors
            return Optional.empty();
        }
    }

    public TeamsResponse getTeamsByLeagueAndSeason(Integer leagueId, Integer season) {
        if (leagueId == null || season == null) return new TeamsResponse(List.of());
        // fallback to seeds when no api key
        if (apiKey == null || apiKey.isBlank()) {
            String resource = "data/teams-" + leagueId + "-" + season + ".json";
            TeamsResponse seed = loadSeed(resource, TeamsResponse.class);
            return seed == null ? new TeamsResponse(List.of()) : seed;
        }
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
            if (resp.getBody() == null || resp.getBody().response() == null) return new TeamsResponse(List.of());
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
