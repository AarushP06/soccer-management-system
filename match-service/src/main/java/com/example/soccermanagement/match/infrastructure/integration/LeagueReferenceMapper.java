package com.example.soccermanagement.match.infrastructure.integration;

import java.util.Optional;
import java.util.UUID;

/**
 * Converts between internal models and transport-friendly data structures.
 */
public final class LeagueReferenceMapper {
    private static final long MATCH_LEAGUE_MARKER = 0x1EA61EA61EA61EA6L;

    private LeagueReferenceMapper() {
    }

    public static UUID toInternalUuid(long leagueServiceId) {
        return new UUID(MATCH_LEAGUE_MARKER, leagueServiceId);
    }

    public static Optional<Long> toLeagueServiceId(UUID internalLeagueId) {
        if (internalLeagueId == null || internalLeagueId.getMostSignificantBits() != MATCH_LEAGUE_MARKER) {
            return Optional.empty();
        }
        long leagueServiceId = internalLeagueId.getLeastSignificantBits();
        return leagueServiceId > 0 ? Optional.of(leagueServiceId) : Optional.empty();
    }
}
