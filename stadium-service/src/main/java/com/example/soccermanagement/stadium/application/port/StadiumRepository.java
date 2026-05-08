package com.example.soccermanagement.stadium.application.port;

import com.example.soccermanagement.stadium.domain.Stadium;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Defines an abstraction used by the application layer in the stadium service.
 */
public interface StadiumRepository {
    List<Stadium> findAll();
    Optional<Stadium> findById(UUID id);
    Stadium save(Stadium aggregate);
    void deleteById(UUID id);
    boolean existsById(UUID id);
    boolean existsByName(String name);
}

