package com.example.soccermanagement.league.application.port;

import com.example.soccermanagement.league.domain.League;

import java.util.List;
import java.util.Optional;

/**
 * Defines an abstraction used by the application layer in the league service.
 */
public interface LeagueRepository {
    League save(League league);
    Optional<League> findById(Long id);
    Optional<League> findByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    void deleteById(Long id);
    boolean existsByName(String name);
    List<League> findAll();
}
