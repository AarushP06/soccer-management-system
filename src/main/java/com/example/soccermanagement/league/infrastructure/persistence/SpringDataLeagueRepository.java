package com.example.soccermanagement.league.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Declares Spring Data persistence operations for the league service.
 */
public interface SpringDataLeagueRepository extends JpaRepository<LeagueJpaEntity, UUID> {
    boolean existsByName(String name);
    Optional<LeagueJpaEntity> findByName(String name);
}
