package com.example.soccermanagement.team.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TeamImportSummary(@Schema(description = "Number of teams imported", example = "20") int imported,
                                 @Schema(description = "Number of teams skipped", example = "0") int skipped) {
}
