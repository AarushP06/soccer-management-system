package com.example.soccermanagement.match.infrastructure.integration;

import com.example.soccermanagement.match.application.dto.LeagueInfo;
import com.example.soccermanagement.match.application.exception.ExternalServiceException;
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
            throw new ExternalServiceException("League service unavailable", ex);
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
            throw new ExternalServiceException("League service unavailable", ex);
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
            throw new ExternalServiceException("League service unavailable", ex);
        }
    }

    @Override
    public Optional<com.example.soccermanagement.match.application.dto.LeagueInfo> findByName(String name) {
        try {
            ResponseEntity<LeagueDto[]> resp = restTemplate.getForEntity(baseUrl + "/api/leagues", LeagueDto[].class);
            LeagueDto[] arr = resp.getBody();
            if (arr == null || arr.length == 0) return Optional.empty();
            for (LeagueDto dto : arr) {
                if (dto == null || dto.getName() == null) continue;
                if (dto.getName().equalsIgnoreCase(name)) {
                    try {
                        return Optional.of(new LeagueInfo(UUID.fromString(dto.getId()), dto.getName(), dto.getExternalCode()));
                    } catch (Exception ex) {
                        // id might be numeric; return with random UUID placeholder
                        return Optional.of(new LeagueInfo(UUID.randomUUID(), dto.getName(), dto.getExternalCode()));
                    }
                }
            }
            return Optional.empty();
        } catch (HttpClientErrorException ex) {
            return Optional.empty();
        } catch (Exception ex) {
            throw new ExternalServiceException("League service unavailable", ex);
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
