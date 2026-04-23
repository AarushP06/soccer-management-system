package com.example.soccermanagement.league.application.port;

import com.example.soccermanagement.league.domain.League;

import java.util.List;
import java.util.Optional;

public interface LeagueRepository {
    League save(League league);
    Optional<League> findById(Long id);
    void deleteById(Long id);
    boolean existsByName(String name);
    List<League> findAll();
}
