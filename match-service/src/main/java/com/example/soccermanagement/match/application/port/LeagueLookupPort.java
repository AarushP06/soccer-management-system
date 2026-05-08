package com.example.soccermanagement.match.application.port;

import java.util.Optional;
import java.util.UUID;

/**
 * Defines an abstraction used by the application layer in the match service.
 */
public interface LeagueLookupPort {
    boolean existsById(UUID leagueId);
    Optional<String> findNameById(UUID leagueId);
    Optional<String> findExternalCodeById(UUID leagueId);
    Optional<com.example.soccermanagement.match.application.dto.LeagueInfo> findByName(String name);
}

