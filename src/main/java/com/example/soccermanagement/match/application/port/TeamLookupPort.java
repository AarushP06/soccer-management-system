package com.example.soccermanagement.match.application.port;

import java.util.Optional;
import java.util.UUID;

/**
 * Defines an abstraction used by the application layer in the match service.
 */
public interface TeamLookupPort {
    boolean existsById(UUID teamId);
    Optional<String> findNameById(UUID teamId);
    Optional<String> findExternalIdById(UUID teamId);
}
