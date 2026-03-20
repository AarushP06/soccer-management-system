package com.example.soccermanagement.match.domain;

import java.util.UUID;

public class MatchSchedule {

    private final UUID id;
    private final UUID leagueId;
    private final String season;
    private final String status;

    private MatchSchedule(UUID id, UUID leagueId, String season, String status) {
        this.id = id;
        this.leagueId = leagueId;
        this.season = season;
        this.status = status;
    }

    public static MatchSchedule create(UUID leagueId, String season) {
        return new MatchSchedule(UUID.randomUUID(), leagueId, season, "OPEN");
    }

    public UUID getId() {
        return id;
    }

    public UUID getLeagueId() {
        return leagueId;
    }

    public String getSeason() {
        return season;
    }

    public String getStatus() {
        return status;
    }
}
