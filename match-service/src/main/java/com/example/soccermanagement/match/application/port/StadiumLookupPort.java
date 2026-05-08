package com.example.soccermanagement.match.application.port;

import java.util.Optional;
import java.util.UUID;

/**
 * Defines an abstraction used by the application layer in the match service.
 */
public interface StadiumLookupPort {
    boolean existsById(UUID stadiumId);
    Optional<String> findNameById(UUID stadiumId);
    Optional<Integer> findExternalVenueIdById(UUID stadiumId);
}

