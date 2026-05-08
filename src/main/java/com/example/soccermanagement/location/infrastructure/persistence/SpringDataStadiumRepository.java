package com.example.soccermanagement.location.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Declares Spring Data persistence operations for the stadium service.
 */
public interface SpringDataStadiumRepository extends JpaRepository<StadiumJpaEntity, UUID> {
    boolean existsByName(String name);
}
