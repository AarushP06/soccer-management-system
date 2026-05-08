package com.example.soccermanagement.location.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents incoming API data for a stadium request.
 */
public record CreateStadiumRequest(@Schema(description = "Name of the stadium", example = "Estádio da Luz") @NotBlank String name) {
}
