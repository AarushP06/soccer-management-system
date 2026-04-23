package com.example.soccermanagement.match.api.dto;

import java.util.UUID;

public record MatchImportSummary(String competitionCode, UUID stadiumId, int imported, int skipped, int missingTeams, int missingLeague) {}

