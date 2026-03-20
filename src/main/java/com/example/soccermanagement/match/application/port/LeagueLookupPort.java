package com.example.soccermanagement.match.application.port;

import java.util.UUID;

public interface LeagueLookupPort {
    boolean existsById(UUID leagueId);
}
