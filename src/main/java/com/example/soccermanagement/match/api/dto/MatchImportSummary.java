package com.example.soccermanagement.match.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record MatchImportSummary(
        @Schema(description = "Competition code used for import", example = "PPL") String competitionCode,
        @Schema(description = "Local stadium id used for imported matches", example = "33333333-3333-3333-3333-333333333333") UUID stadiumId,
        @Schema(description = "Number of matches imported", example = "8") int imported,
        @Schema(description = "Number of matches skipped", example = "12") int skipped,
        @Schema(description = "Number of matches skipped due to missing teams", example = "3") int missingTeams,
        @Schema(description = "Number of competitions skipped due to missing league", example = "0") int missingLeague
) {
}