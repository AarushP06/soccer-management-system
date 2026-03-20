package com.example.soccermanagement.shared.infrastructure.client;

import com.example.soccermanagement.shared.config.FootballDataProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
        return restClient.get().uri(uri).retrieve().body(String.class);
    }
}
