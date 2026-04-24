package com.example.soccermanagement.match.infrastructure.integration;

import com.example.soccermanagement.match.application.dto.TeamInfo;
import com.example.soccermanagement.match.application.exception.ExternalServiceException;
import com.example.soccermanagement.match.application.port.TeamLookupPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Component
public class TeamServiceClient implements TeamLookupPort {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public TeamServiceClient(@Value("${services.team.url:http://localhost:8083}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    @Override
    public boolean existsById(UUID teamId) {
        try {
            ResponseEntity<TeamDto> resp = restTemplate.getForEntity(baseUrl + "/api/teams/{id}", TeamDto.class, teamId);
            return resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null;
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) return false;
            return false;
        } catch (Exception ex) {
            throw new ExternalServiceException("Team service unavailable", ex);
        }
    }

    @Override
    public Optional<String> findNameById(UUID teamId) {
        try {
            ResponseEntity<TeamDto> resp = restTemplate.getForEntity(baseUrl + "/api/teams/{id}", TeamDto.class, teamId);
            TeamDto body = resp.getBody();
            return body == null ? Optional.empty() : Optional.ofNullable(body.getName());
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) return Optional.empty();
            return Optional.empty();
        } catch (Exception ex) {
            throw new ExternalServiceException("Team service unavailable", ex);
        }
    }

    @Override
    public Optional<String> findExternalIdById(UUID teamId) {
        try {
            ResponseEntity<TeamDto> resp = restTemplate.getForEntity(baseUrl + "/api/teams/{id}", TeamDto.class, teamId);
            TeamDto body = resp.getBody();
            return body == null ? Optional.empty() : Optional.ofNullable(body.getExternalId());
        } catch (Exception ex) {
            throw new ExternalServiceException("Team service unavailable", ex);
        }
    }

    @Override
    public Optional<com.example.soccermanagement.match.application.dto.TeamInfo> findByName(String name) {
        try {
            ResponseEntity<TeamDto[]> resp = restTemplate.getForEntity(baseUrl + "/api/teams?name={name}", TeamDto[].class, name);
            TeamDto[] arr = resp.getBody();
            if (arr == null || arr.length == 0) return Optional.empty();
            TeamDto first = arr[0];
            return Optional.of(new TeamInfo(UUID.fromString(first.getId()), first.getName(), first.getExternalId()));
        } catch (Exception ex) {
            throw new ExternalServiceException("Team service unavailable", ex);
        }
    }

    private static final class TeamDto {
        public String id;
        public String name;
        public String externalId;

        public String getId() { return id; }
        public String getName() { return name; }
        public String getExternalId() { return externalId; }
    }
}
