package com.example.soccermanagement.stadium.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents incoming API data for a stadium request.
 */
public record CreateStadiumRequest(@NotBlank String name) {}

