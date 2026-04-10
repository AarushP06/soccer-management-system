package com.example.soccermanagement.match.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.RepresentationModel;

import java.util.UUID;

public class MatchDetailsResponse extends RepresentationModel<MatchDetailsResponse> {

    @Schema(description = "Local match id", example = "44444444-4444-4444-4444-444444444444")
    private UUID id;
    @Schema(description = "Local league id", example = "11111111-1111-1111-1111-111111111111")
    private UUID leagueId;
    @Schema(description = "Local home team id", example = "22222222-2222-2222-2222-222222222221")
    private UUID homeTeamId;
    @Schema(description = "Local away team id", example = "22222222-2222-2222-2222-222222222222")
    private UUID awayTeamId;
    @Schema(description = "Local stadium id", example = "33333333-3333-3333-3333-333333333333")
    private UUID stadiumId;
    @Schema(description = "Match status", example = "SCHEDULED")
    private String status;

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
}
