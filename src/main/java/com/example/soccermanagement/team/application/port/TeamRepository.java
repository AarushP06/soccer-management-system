package com.example.soccermanagement.team.application.port;

import com.example.soccermanagement.team.domain.Team;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository {
    List<Team> findAll();
    Optional<Team> findById(UUID id);
    Team save(Team aggregate);
    void deleteById(UUID id);
    boolean existsByName(String name);
}
