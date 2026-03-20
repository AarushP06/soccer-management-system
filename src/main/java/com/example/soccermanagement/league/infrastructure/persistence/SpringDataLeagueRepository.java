package com.example.soccermanagement.league.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataLeagueRepository extends JpaRepository<LeagueJpaEntity, UUID> {
}
