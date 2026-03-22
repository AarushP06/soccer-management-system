package com.example.soccermanagement.team.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataTeamRepository extends JpaRepository<TeamJpaEntity, UUID> {
    boolean existsByName(String name);
    Optional<TeamJpaEntity> findByName(String name);
}
