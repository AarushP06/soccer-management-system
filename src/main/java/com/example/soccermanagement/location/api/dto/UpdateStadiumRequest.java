package com.example.soccermanagement.location.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents incoming API data for a stadium request.
 */
public record UpdateStadiumRequest(@NotBlank String name) {
}
