package com.example.soccermanagement.match.infrastructure.integration;

import com.example.soccermanagement.match.application.dto.LeagueInfo;
import com.example.soccermanagement.match.application.port.LeagueLookupPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Component
public class LeagueServiceClient implements LeagueLookupPort {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public LeagueServiceClient(@Value("${services.league.url:http://localhost:8082}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    @Override
    public boolean existsById(UUID leagueId) {
        try {
            ResponseEntity<LeagueDto> resp = restTemplate.getForEntity(baseUrl + "/api/leagues/{id}", LeagueDto.class, leagueId);
            return resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null;
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) return false;
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public Optional<String> findNameById(UUID leagueId) {
        try {
            ResponseEntity<LeagueDto> resp = restTemplate.getForEntity(baseUrl + "/api/leagues/{id}", LeagueDto.class, leagueId);
            LeagueDto body = resp.getBody();
            return body == null ? Optional.empty() : Optional.ofNullable(body.getName());
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) return Optional.empty();
            return Optional.empty();
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> findExternalCodeById(UUID leagueId) {
        try {
            ResponseEntity<LeagueDto> resp = restTemplate.getForEntity(baseUrl + "/api/leagues/{id}", LeagueDto.class, leagueId);
            LeagueDto body = resp.getBody();
            return body == null ? Optional.empty() : Optional.ofNullable(body.getExternalCode());
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) return Optional.empty();
            return Optional.empty();
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<com.example.soccermanagement.match.application.dto.LeagueInfo> findByName(String name) {
        try {
            ResponseEntity<LeagueDto[]> resp = restTemplate.getForEntity(baseUrl + "/api/leagues?name={name}", LeagueDto[].class, name);
            LeagueDto[] arr = resp.getBody();
            if (arr == null || arr.length == 0) return Optional.empty();
            LeagueDto first = arr[0];
            return Optional.of(new LeagueInfo(UUID.fromString(first.getId()), first.getName(), first.getExternalCode()));
        } catch (HttpClientErrorException ex) {
            return Optional.empty();
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private static final class LeagueDto {
        public String id;
        public String name;
        public String externalCode;

        public String getId() { return id; }
        public String getName() { return name; }
        public String getExternalCode() { return externalCode; }
    }
}
