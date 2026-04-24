package com.example.soccermanagement.match.infrastructure.integration;

import com.example.soccermanagement.match.application.exception.ExternalServiceException;
import com.example.soccermanagement.match.application.port.StadiumLookupPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Component
public class StadiumServiceClient implements StadiumLookupPort {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public StadiumServiceClient(@Value("${services.stadium.url:http://localhost:8084}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    @Override
    public boolean existsById(UUID stadiumId) {
        try {
            ResponseEntity<StadiumDto> resp = restTemplate.getForEntity(baseUrl + "/api/stadiums/{id}", StadiumDto.class, stadiumId);
            return resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null;
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) return false;
            return false;
        } catch (Exception ex) {
            throw new ExternalServiceException("Stadium service unavailable", ex);
        }
    }

    @Override
    public Optional<String> findNameById(UUID stadiumId) {
        try {
            ResponseEntity<StadiumDto> resp = restTemplate.getForEntity(baseUrl + "/api/stadiums/{id}", StadiumDto.class, stadiumId);
            StadiumDto body = resp.getBody();
            return body == null ? Optional.empty() : Optional.ofNullable(body.getName());
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) return Optional.empty();
            return Optional.empty();
        } catch (Exception ex) {
            throw new ExternalServiceException("Stadium service unavailable", ex);
        }
    }

    @Override
    public Optional<Integer> findExternalVenueIdById(UUID stadiumId) {
        try {
            ResponseEntity<StadiumDto> resp = restTemplate.getForEntity(baseUrl + "/api/stadiums/{id}", StadiumDto.class, stadiumId);
            StadiumDto body = resp.getBody();
            return body == null ? Optional.empty() : Optional.ofNullable(body.getExternalVenueId());
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) return Optional.empty();
            return Optional.empty();
        } catch (Exception ex) {
            throw new ExternalServiceException("Stadium service unavailable", ex);
        }
    }

    private static final class StadiumDto {
        public String id;
        public String name;
        public Integer externalVenueId;

        public String getId() { return id; }
        public String getName() { return name; }
        public Integer getExternalVenueId() { return externalVenueId; }
    }
}
