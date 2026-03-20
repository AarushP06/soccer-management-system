package com.example.soccermanagement.match.infrastructure.integration;

import com.example.soccermanagement.match.application.port.TeamLookupPort;
import com.example.soccermanagement.team.infrastructure.persistence.SpringDataTeamRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TeamLookupAdapter implements TeamLookupPort {

    private final SpringDataTeamRepository repository;

    public TeamLookupAdapter(SpringDataTeamRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsById(UUID teamId) {
        return repository.existsById(teamId);
    }
}
