package com.example.soccermanagement.match.infrastructure.integration;

import com.example.soccermanagement.league.infrastructure.persistence.SpringDataLeagueRepository;
import com.example.soccermanagement.match.application.port.LeagueLookupPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LeagueLookupAdapter implements LeagueLookupPort {

    private final SpringDataLeagueRepository repository;

    public LeagueLookupAdapter(SpringDataLeagueRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsById(UUID leagueId) {
        return repository.existsById(leagueId);
    }
}
