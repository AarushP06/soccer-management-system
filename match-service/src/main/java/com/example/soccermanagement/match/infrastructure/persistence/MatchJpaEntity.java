package com.example.soccermanagement.match.infrastructure.persistence;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "matches")
public class MatchJpaEntity {
    @Id
    private UUID id;
    private UUID leagueId;
    private UUID homeTeamId;
    private UUID awayTeamId;
    private UUID stadiumId;
    private String externalMatchId;
    private String status;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getLeagueId() { return leagueId; }
    public void setLeagueId(UUID leagueId) { this.leagueId = leagueId; }
    public UUID getHomeTeamId() { return homeTeamId; }
    public void setHomeTeamId(UUID homeTeamId) { this.homeTeamId = homeTeamId; }
    public UUID getAwayTeamId() { return awayTeamId; }
    public void setAwayTeamId(UUID awayTeamId) { this.awayTeamId = awayTeamId; }
    public UUID getStadiumId() { return stadiumId; }
    public void setStadiumId(UUID stadiumId) { this.stadiumId = stadiumId; }
    public String getExternalMatchId() { return externalMatchId; }
    public void setExternalMatchId(String externalMatchId) { this.externalMatchId = externalMatchId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

