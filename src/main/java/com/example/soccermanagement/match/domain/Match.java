package com.example.soccermanagement.match.domain;

import com.example.soccermanagement.shared.domain.DomainException;

import java.util.UUID;

public class Match {

    private final UUID id;
    private final UUID leagueId;
    private final UUID homeTeamId;
    private final UUID awayTeamId;
    private final UUID stadiumId;
    private String status;

    private Match(UUID id, UUID leagueId, UUID homeTeamId, UUID awayTeamId, UUID stadiumId, String status) {
        if (homeTeamId.equals(awayTeamId)) {
            throw new DomainException("Home team and away team must be different");
        }
        this.id = id;
        this.leagueId = leagueId;
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
        this.stadiumId = stadiumId;
        this.status = status;
    }

    public static Match create(UUID leagueId, UUID homeTeamId, UUID awayTeamId, UUID stadiumId) {
        return new Match(UUID.randomUUID(), leagueId, homeTeamId, awayTeamId, stadiumId, "SCHEDULED");
    }

    public static Match rehydrate(UUID id, UUID leagueId, UUID homeTeamId, UUID awayTeamId, UUID stadiumId, String status) {
        return new Match(id, leagueId, homeTeamId, awayTeamId, stadiumId, status);
    }

    public UUID getId() {
        return id;
    }

    public UUID getLeagueId() {
        return leagueId;
    }

    public UUID getHomeTeamId() {
        return homeTeamId;
    }

    public UUID getAwayTeamId() {
        return awayTeamId;
    }

    public UUID getStadiumId() {
        return stadiumId;
    }

    public String getStatus() {
        return status;
    }
}
