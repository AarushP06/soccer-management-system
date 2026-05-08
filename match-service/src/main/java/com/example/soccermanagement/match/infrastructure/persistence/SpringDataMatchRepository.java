package com.example.soccermanagement.match.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Declares Spring Data persistence operations for the match service.
 */
public interface SpringDataMatchRepository extends JpaRepository<MatchJpaEntity, UUID> {
    boolean existsByLeagueIdAndHomeTeamIdAndAwayTeamId(UUID leagueId, UUID homeTeamId, UUID awayTeamId);
}

