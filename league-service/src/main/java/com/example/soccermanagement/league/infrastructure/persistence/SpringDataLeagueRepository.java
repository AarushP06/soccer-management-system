package com.example.soccermanagement.league.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Declares Spring Data persistence operations for the league service.
 */
public interface SpringDataLeagueRepository extends JpaRepository<LeagueEntity, Long> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    java.util.Optional<LeagueEntity> findByName(String name);
}

