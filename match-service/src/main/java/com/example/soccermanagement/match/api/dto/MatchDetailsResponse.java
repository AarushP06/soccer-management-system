package com.example.soccermanagement.match.api.dto;

import org.springframework.hateoas.RepresentationModel;

import java.util.UUID;

public class MatchDetailsResponse extends RepresentationModel<MatchDetailsResponse> {
    private UUID id;
    private UUID leagueId;
    private UUID homeTeamId;
    private UUID awayTeamId;
    private UUID stadiumId;
    private String status;
    private String externalMatchId;

    public MatchDetailsResponse(UUID id, UUID leagueId, UUID homeTeamId, UUID awayTeamId, UUID stadiumId, String status) {
        this.id = id;
        this.leagueId = leagueId;
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
        this.stadiumId = stadiumId;
        this.status = status;
    }

    public UUID getId() { return id; }
    public UUID getLeagueId() { return leagueId; }
    public UUID getHomeTeamId() { return homeTeamId; }
    public UUID getAwayTeamId() { return awayTeamId; }
    public UUID getStadiumId() { return stadiumId; }
    public String getStatus() { return status; }
    public String getExternalMatchId() { return externalMatchId; }
    public void setExternalMatchId(String externalMatchId) { this.externalMatchId = externalMatchId; }
}

