package com.example.soccermanagement.league.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataLeagueRepository extends JpaRepository<LeagueEntity, Long> {
    boolean existsByName(String name);
}

