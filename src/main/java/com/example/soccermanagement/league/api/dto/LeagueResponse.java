package com.example.soccermanagement.league.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record LeagueResponse(@Schema(description = "Local league id", example = "11111111-1111-1111-1111-111111111111") UUID id,
                             @Schema(description = "Local league name", example = "Premier League") String name,
                             @Schema(description = "External competition code (football-data)", example = "PL") String externalCode) {
}
