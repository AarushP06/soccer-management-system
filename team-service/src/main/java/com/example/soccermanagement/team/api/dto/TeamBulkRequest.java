package com.example.soccermanagement.team.api.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents incoming API data for a team request.
 */
public record TeamBulkRequest(UUID id, @NotBlank String name) { }

