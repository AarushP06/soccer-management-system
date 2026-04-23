package com.example.soccermanagement.match.domain;

import com.example.soccermanagement.match.domain.exception.MatchValidationException;

import java.util.UUID;

public class Match {
    private final UUID id;
    private final UUID leagueId;
    private final UUID homeTeamId;
    private final UUID awayTeamId;
    private final UUID stadiumId;
    private String externalMatchId;
    private String status;

    private Match(UUID id, UUID leagueId, UUID homeTeamId, UUID awayTeamId, UUID stadiumId, String externalMatchId, String status) {
        this.id = id;
        this.leagueId = leagueId;
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
        this.stadiumId = stadiumId;
        this.externalMatchId = externalMatchId;
        this.status = status;
    }

    public static Match create(UUID leagueId, UUID homeTeamId, UUID awayTeamId, UUID stadiumId) {
        validateTeams(homeTeamId, awayTeamId);
        return new Match(UUID.randomUUID(), leagueId, homeTeamId, awayTeamId, stadiumId, null, "SCHEDULED");
    }

    public static Match createFromExternal(UUID leagueId, UUID homeTeamId, UUID awayTeamId, UUID stadiumId, String externalMatchId) {
        validateTeams(homeTeamId, awayTeamId);
        return new Match(UUID.randomUUID(), leagueId, homeTeamId, awayTeamId, stadiumId, externalMatchId, "SCHEDULED");
    }

    private static void validateTeams(UUID homeTeamId, UUID awayTeamId) {
        if (homeTeamId == null || awayTeamId == null) {
            throw new MatchValidationException("Team ids must not be null");
        }
        if (homeTeamId.equals(awayTeamId)) {
            throw new MatchValidationException("Home and away teams must be different");
        }
    }

    public static Match rehydrate(UUID id, UUID leagueId, UUID homeTeamId, UUID awayTeamId, UUID stadiumId, String externalMatchId, String status) {
        return new Match(id, leagueId, homeTeamId, awayTeamId, stadiumId, externalMatchId, status);
    }

    public UUID getId() { return id; }
    public UUID getLeagueId() { return leagueId; }
    public UUID getHomeTeamId() { return homeTeamId; }
    public UUID getAwayTeamId() { return awayTeamId; }
    public UUID getStadiumId() { return stadiumId; }
    public String getExternalMatchId() { return externalMatchId; }
    public String getStatus() { return status; }
}

