package com.example.soccermanagement.location.infrastructure.integration;
import com.example.soccermanagement.shared.config.ApiFootballProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
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
    public record VenueResponse(java.util.List<ExternalVenue> response) {
    }
    public record ExternalVenue(
            Integer id,
            String name,
            String city,
            String country,
            Integer capacity
    ) {
    }
}