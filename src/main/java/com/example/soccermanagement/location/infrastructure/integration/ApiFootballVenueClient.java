package com.example.soccermanagement.location.infrastructure.integration;

import com.example.soccermanagement.shared.config.ApiFootballProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Optional;

@Component
public class ApiFootballVenueClient {
    private final RestClient restClient;

    public ApiFootballVenueClient(ApiFootballProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .defaultHeader("x-apisports-key", properties.apiKey())
                .build();
    }

    public Optional<ExternalVenue> getVenueById(Integer venueId) {
        try {
            VenueResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/venues")
                            .queryParam("id", venueId)
                            .build())
                    .retrieve()
                    .body(VenueResponse.class);
            if (response != null && response.response() != null && !response.response().isEmpty()) {
                return Optional.of(response.response().get(0));
            }
            return Optional.empty();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    public TeamsResponse getTeamsByLeagueAndSeason(Integer leagueId, Integer season) {
        if (leagueId == null || season == null) {
            return new TeamsResponse(List.of());
        }
        try {
            TeamsResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/teams")
                            .queryParam("league", leagueId)
                            .queryParam("season", season)
                            .build())
                    .retrieve()
                    .body(TeamsResponse.class);
            if (response == null || response.response() == null) {
                return new TeamsResponse(List.of());
            }
            return response;
        } catch (RestClientResponseException ex) {
            // treat 404 or other client errors as empty result for import convenience
            return new TeamsResponse(List.of());
        }
    }

    public record VenueResponse(java.util.List<ExternalVenue> response) {
    }

    public record TeamsResponse(java.util.List<Team> response) {
    }

    public record ExternalVenue(
            Integer id,
            String name,
            String city,
            String country,
            Integer capacity
    ) {
    }

    public record Team(
            Integer id,
            String name,
            ExternalVenue venue
    ) {
    }
}