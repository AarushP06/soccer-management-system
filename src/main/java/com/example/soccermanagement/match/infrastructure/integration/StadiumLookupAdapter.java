package com.example.soccermanagement.match.infrastructure.integration;

import com.example.soccermanagement.location.infrastructure.persistence.SpringDataStadiumRepository;
import com.example.soccermanagement.match.application.port.StadiumLookupPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StadiumLookupAdapter implements StadiumLookupPort {

    private final SpringDataStadiumRepository repository;

    public StadiumLookupAdapter(SpringDataStadiumRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsById(UUID stadiumId) {
        return repository.existsById(stadiumId);
    }
}
