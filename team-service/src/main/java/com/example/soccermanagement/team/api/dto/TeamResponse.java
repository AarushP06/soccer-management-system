package com.example.soccermanagement.team.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record TeamResponse(@Schema(description = "Local team id", example = "22222222-2222-2222-2222-222222222222") UUID id,
                           @Schema(description = "Local team name", example = "Benfica") String name,
                           @Schema(description = "External team id (football-data)", example = "65") String externalId) {
}

