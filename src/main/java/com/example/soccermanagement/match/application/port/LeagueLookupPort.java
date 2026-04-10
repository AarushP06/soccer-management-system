package com.example.soccermanagement.match.application.port;

import java.util.Optional;
import java.util.UUID;

public interface LeagueLookupPort {
    boolean existsById(UUID leagueId);
    Optional<String> findNameById(UUID leagueId);
    Optional<String> findExternalCodeById(UUID leagueId);
}
