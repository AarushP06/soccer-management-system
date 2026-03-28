package com.example.soccermanagement.shared.infrastructure.client;

import com.example.soccermanagement.shared.config.FootballDataProperties;
import com.example.soccermanagement.shared.exception.ExternalApiRateLimitException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class FootballDataBaseClient {

    private final RestClient restClient;

    public FootballDataBaseClient(FootballDataProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .defaultHeader("X-Auth-Token", properties.apiToken())
                .build();
    }

    public String get(String uri) {
        try {
            return restClient.get().uri(uri).retrieve().body(String.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 429) {
                throw new ExternalApiRateLimitException("External API rate limit exceeded", ex);
            }
            throw ex;
        }
    }

    public <T> T get(String uri, Class<T> responseType) {
        try {
            return restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(responseType);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 429) {
                throw new ExternalApiRateLimitException("External API rate limit exceeded", ex);
            }
            throw ex;
        }
    }

    public <T> T get(String uri, Class<T> responseType, String pathVariable) {
        try {
            return restClient.get()
                    .uri(uri, pathVariable)
                    .retrieve()
                    .body(responseType);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 429) {
                throw new ExternalApiRateLimitException("External API rate limit exceeded", ex);
            }
            throw ex;
        }
    }
}
