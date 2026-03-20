package com.example.soccermanagement.team.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataTeamRepository extends JpaRepository<TeamJpaEntity, UUID> {
}
