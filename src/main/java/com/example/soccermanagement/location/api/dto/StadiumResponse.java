package com.example.soccermanagement.location.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record StadiumResponse(
        @Schema(description = "Local stadium id", example = "33333333-3333-3333-3333-333333333333") UUID id,
        @Schema(description = "Local stadium name", example = "Estádio da Luz") String name,
        @Schema(description = "External venue id from API-Football", example = "670") Integer externalVenueId,
        @Schema(description = "Stadium city (from external source)", example = "Lisbon") String city,
        @Schema(description = "Stadium country (from external source)", example = "Portugal") String country,
        @Schema(description = "Stadium capacity (from external source)", example = "65000") Integer capacity
) {
}
