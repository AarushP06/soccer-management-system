package com.example.soccermanagement.match.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents incoming API data for a match request.
 */
public record UpdateMatchRequest(@NotBlank String status) { }

