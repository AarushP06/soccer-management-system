package com.example.soccermanagement.league.application.port;

import com.example.soccermanagement.league.domain.League;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeagueRepository {
    List<League> findAll();
    Optional<League> findById(UUID id);
    League save(League aggregate);
    void deleteById(UUID id);
    boolean existsByName(String name);
    Optional<League> findByName(String name);
}
